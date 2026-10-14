"""Backend tests. The model call is replaced, so no API key or network is needed."""
import json

import pytest

from app import QuizFormatError, create_app, normalize_quiz


def q(question="2+2?", options=("3", "4", "5", "6"), answer="B"):
    return {"question": question, "options": list(options), "correct_answer": answer}


def test_normalize_accepts_valid_quiz():
    assert normalize_quiz({"quiz": [q()]}) == [q()]


def test_normalize_parses_json_text_and_bare_lists():
    assert normalize_quiz(json.dumps({"quiz": [q()]})) == [q()]
    assert normalize_quiz([q()]) == [q()]


def test_normalize_converts_answer_text_and_formats_to_letter():
    assert normalize_quiz([q(answer="4")])[0]["correct_answer"] == "B"
    assert normalize_quiz([q(answer="b)")])[0]["correct_answer"] == "B"


def test_normalize_drops_questions_the_app_cannot_show():
    good = q(question="ok?")
    bad = [
        q(options=("1", "2", "3")),          # 3 options would crash the quiz screen
        q(options=("1", "1", "2", "3")),     # duplicate options
        q(answer="E"),                       # answer not one of the options
        q(question=" "),                     # empty question
        "not a dict",
    ]
    assert normalize_quiz(bad + [good]) == [good]


def test_normalize_rejects_unusable_output():
    for raw in ["not json", {"nope": []}, {"quiz": [q(answer="Z")]}]:
        with pytest.raises(QuizFormatError):
            normalize_quiz(raw)


@pytest.fixture
def make_client():
    def _make(generate):
        return create_app(generate=generate).test_client()
    return _make


def test_get_quiz_returns_app_format_and_passes_topic_and_count(make_client):
    calls = []

    def fake(topic, count):
        calls.append((topic, count))
        return json.dumps({"quiz": [q()] * count})

    r = make_client(fake).get("/getQuiz?topic=Java&count=3")
    assert r.status_code == 200
    assert r.get_json() == {"quiz": [q()] * 3}
    assert calls == [("Java", 3)]


def test_get_quiz_validates_input(make_client):
    client = make_client(lambda t, c: pytest.fail("model should not be called"))
    assert client.get("/getQuiz").status_code == 400
    assert client.get("/getQuiz?topic=" + "x" * 101).status_code == 400
    assert client.get("/getQuiz?topic=Java&count=abc").status_code == 400


def test_count_is_clamped(make_client):
    seen = []
    client = make_client(lambda t, c: seen.append(c) or {"quiz": [q()]})
    client.get("/getQuiz?topic=x&count=500")
    client.get("/getQuiz?topic=x&count=0")
    assert seen == [10, 1]


def test_bad_model_output_is_a_502_not_a_crash(make_client):
    r = make_client(lambda t, c: "sorry, I can't").get("/getQuiz?topic=Java")
    assert r.status_code == 502
    assert "error" in r.get_json()


def test_health(make_client):
    assert make_client(lambda t, c: None).get("/health").get_json() == {"status": "ok"}
