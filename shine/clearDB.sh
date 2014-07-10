#!/bin/bash

psql -d shine -U shine -f dropSchema.sql
echo PostgreSQL schema clean up completed. 
