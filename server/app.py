"""QuizLLM backend: generates multiple-choice quizzes with Gemini.

GET /getQuiz?topic=<text>&count=<1-10>
  200 {"quiz": [{"question": str, "options": [4 x str], "correct_answer": "A"|"B"|"C"|"D"}]}
  400 {"error": ...}  bad input
  502 {"error": ...}  the model's reply could not be turned into a valid quiz

The Android app calls this from the emulator at http://10.0.2.2:5001/.
"""
import json
import logging
import os

from flask import Flask, jsonify, request

log = logging.getLogger("quizllm")

MODEL = os.environ.get("GEMINI_MODEL", "gemini-2.5-flash")
DEFAULT_COUNT = 5
MAX_COUNT = 10
MAX_TOPIC_LEN = 100
LETTERS = ["A", "B", "C", "D"]

PROMPT = """Write {count} multiple-choice quiz questions about: {topic}

Rules:
- Each question has exactly 4 options and exactly one correct option.
- Options are short and plausible; do not prefix them with letters.
- correct_answer is the letter (A, B, C or D) of the correct option.
- Vary which letter is correct.
- If the topic is unclear, choose its most common meaning."""

# JSON schema passed to Gemini so the reply is structured, not free text.
QUIZ_SCHEMA = {
    "type": "object",
    "properties": {
        "quiz": {
            "type": "array",
            "items": {
                "type": "object",
                "properties": {
                    "question": {"type": "string"},
                    "options": {"type": "array", "items": {"type": "string"}},
                    "correct_answer": {"type": "string", "enum": LETTERS},
                },
                "required": ["question", "options", "correct_answer"],
            },
        }
    },
    "required": ["quiz"],
}


class QuizFormatError(ValueError):
    """The model's output is not a usable quiz."""


def normalize_quiz(raw):
    """Validate model output and return a clean list of questions.

    Drops malformed questions rather than sending them to the app, where a
    question with fewer than 4 options would crash the quiz screen. Accepts a
    correct answer given as the option text and converts it to its letter.
    """
    if isinstance(raw, str):
        try:
            raw = json.loads(raw)
        except json.JSONDecodeError as e:
            raise QuizFormatError(f"not JSON: {e}") from e
    items = raw.get("quiz") if isinstance(raw, dict) else raw
    if not isinstance(items, list):
        raise QuizFormatError("missing 'quiz' list")

    clean = []
    for item in items:
        if not isinstance(item, dict):
            continue
        question = str(item.get("question", "")).strip()
        options = item.get("options")
        answer = str(item.get("correct_answer", "")).strip()
        if not question or not isinstance(options, list) or len(options) != 4:
            continue
        options = [str(o).strip() for o in options]
        if any(not o for o in options) or len(set(options)) != 4:
            continue
        letter = answer.upper().rstrip(".)") if len(answer) <= 2 else ""
        if letter not in LETTERS:
            letter = LETTERS[options.index(answer)] if answer in options else ""
        if not letter:
            continue
        clean.append({"question": question, "options": options, "correct_answer": letter})

    if not clean:
        raise QuizFormatError("no valid questions in model output")
    return clean


def generate_with_gemini(topic, count):
    """Call Gemini with a JSON response schema and return the raw JSON text."""
    from google import genai  # imported lazily so tests run without the SDK configured

    client = genai.Client(api_key=os.environ["GEMINI_API_KEY"])
    response = client.models.generate_content(
        model=MODEL,
        contents=PROMPT.format(topic=topic, count=count),
        config={
            "response_mime_type": "application/json",
            "response_schema": QUIZ_SCHEMA,
            "temperature": 0.7,
        },
    )
    return response.text


def create_app(generate=generate_with_gemini):
    app = Flask(__name__)

    @app.get("/health")
    def health():
        return jsonify(status="ok")

    @app.get("/getQuiz")
    def get_quiz():
        topic = (request.args.get("topic") or "").strip()
        if not topic:
            return jsonify(error="topic is required"), 400
        if len(topic) > MAX_TOPIC_LEN:
            return jsonify(error=f"topic must be at most {MAX_TOPIC_LEN} characters"), 400
        try:
            count = int(request.args.get("count", DEFAULT_COUNT))
        except ValueError:
            return jsonify(error="count must be a number"), 400
        count = max(1, min(count, MAX_COUNT))

        try:
            quiz = normalize_quiz(generate(topic, count))
        except QuizFormatError as e:
            log.warning("bad quiz for topic %r: %s", topic, e)
            return jsonify(error="could not generate a valid quiz, please try again"), 502
        return jsonify(quiz=quiz[:count])

    return app


app = create_app()

if __name__ == "__main__":
    if not os.environ.get("GEMINI_API_KEY"):
        raise SystemExit("Set GEMINI_API_KEY first (Google AI Studio).")
    # 0.0.0.0 so the Android emulator (10.0.2.2) and phones on the same Wi-Fi can reach it.
    app.run(host="0.0.0.0", port=int(os.environ.get("PORT", 5001)))
