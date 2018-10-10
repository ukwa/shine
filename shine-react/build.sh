./node_modules/.bin/browserify index.js \
		--require react \
		--require react-dom \
		--transform [ babelify --presets [ react es2015 stage-2 ] ] \
		--standalone FacetedSearch \
		-o web.js
