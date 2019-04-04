deploy:
	git push heroku master

jar:
	lein uberjar

hlog:
	heroku logs --tail

hrepl:
	heroku run lein repl
