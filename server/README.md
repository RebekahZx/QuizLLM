# QuizLLM server

A Flask API that asks Gemini for multiple-choice questions on a topic and returns them in the
format the Android app reads.

```bash
cd server
python -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt
export GEMINI_API_KEY=...        # https://aistudio.google.com/apikey
python app.py                    # serves on 0.0.0.0:5001
curl "http://localhost:5001/getQuiz?topic=Photosynthesis&count=3"
```

Tests (no API key needed): `pip install pytest && python -m pytest -q tests`
