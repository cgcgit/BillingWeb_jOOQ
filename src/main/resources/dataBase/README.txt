# -----------------------------
# Database System
# -----------------------------

Postgresql 9.6.5


# -----------------------------
# Database for the applicatioin
# -----------------------------

name: db_billing
schema: billing


# -----------------------------
# User's database
# -----------------------------

#  User owner of the database
user: billing_admin
password: billing_admin

# Application's user
user: billing_app
password: billing_app


# -----------------------------
# Dump database
# -----------------------------

DDL: db_billing.sql
DATA: db_billing_data.sql



