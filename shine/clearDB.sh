#!/bin/bash

/cygdrive/c/Program\ Files/PostgreSQL/9.3/bin/psql.exe -d shine -U shine -f dropSchema.sql
echo PostgreSQL schema clean up completed. 
