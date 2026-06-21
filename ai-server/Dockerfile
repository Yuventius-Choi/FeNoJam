FROM python:3.11-slim

WORKDIR /ai

RUN apt-get update && apt-get install -y gcc && rm -rf /var/lib/apt/lists/*

# 자신의 환경에 맞는 버전으로 수정하세여!
RUN pip install torch torchvision --index-url https://download.pytorch.org/whl/cu121

COPY requirements.txt .

RUN pip install --no-cache-dir -r requirements.txt

COPY src src

EXPOSE 8000

ENV PYTHONPATH=/ai/src

CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]