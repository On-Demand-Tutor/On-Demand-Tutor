#!/bin/sh
echo "Waiting for MySQL"
until nc -z -v -w30 $DB_HOST $DB_PORT
do
  echo "Waiting for database connection"
  sleep 5
done

echo "Running migrations"
python manage.py migrate

echo "Starting server"
gunicorn rating_feedback.wsgi:application --bind 0.0.0.0:8000