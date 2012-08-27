function Schelling(canvasId, worldSize){

	this.canvas = document.querySelector('#' + canvasId);
	this.ctxt = this.canvas.getContext('2d');
	this.canvasSize = this.ctxt.canvas.width;

	this.worldSize = worldSize;
	this.cellSize = this.canvasSize /this.worldSize;
	this.cells = [];

	this.neighborhoodRadius = 3;
	this.happinessThreshold = 0.5;

	// Initialize cells.
	for(var i = 0; i < this.worldSize; ++i) {

		this.cells[i] = [];

		for(var j = 0; j < this.worldSize; ++j)
			this.cells[i][j] = {
				x: j,
				y: i,
				type: Math.random() < 0.5 ? 0 : 1
			};
	}

	// Precompute neighbors.
	for(var i = 0; i < this.worldSize; ++i)
		for(var j = 0; j < this.worldSize; ++j)
			this.cells[i][j].n = this.neighborhood(this.cells[i][j]);

	this.draw();

	this.playing = false;

	// GO!
	var t = this;
	this.listener = this.canvas.addEventListener('click', function(){ t.toggle(); }, false);

};

Schelling.prototype ={

	toggle: function() {

		var t = this;

		if(!this.playing)
			this.interval = setInterval(function(){ t.tick(); }, 100);
		else
			clearInterval(this.interval);

		this.playing = !this.playing;
	},

	tick: function() {

		this.update();
		this.draw();
	},

	update: function() {

		var unhappy = [];

		// Check for unhappy this.cells.
		for(var i = 0; i < this.worldSize; ++i)
			for(var j = 0; j < this.worldSize; ++j)
				if(this.isUnhappy(this.cells[i][j]))
					unhappy.push(this.cells[i][j]);

		// Switch cell types.
		while(unhappy.length > 1) {

			var i1, i2;

			do {
				var i1 = Math.floor(Math.random() * unhappy.length);
				var i2 = Math.floor(Math.random() * unhappy.length);
			} while(i1 == i2);

			var i1 = Math.floor(Math.random() * unhappy.length);
			var i2 = Math.floor(Math.random() * unhappy.length);

			var t = unhappy[i1].type;
			unhappy[i1].type = unhappy[i2].type;
			unhappy[i2].type = t;

			unhappy.splice(i1, 1);
			unhappy.splice(i2, 1);
		}
	},

	neighborhood: function(cell) {

		var neighbors = [];

		var distance = function(c1, c2) {
			return Math.sqrt(Math.pow(c1.x - c2.x, 2) + Math.pow(c1.y - c2.y, 2))
		}

		for(var i = 0; i < this.worldSize; ++i)
			for(var j = 0; j < this.worldSize; ++j){

				if(i == cell.y && j == cell.x)
					continue;

				if(distance(cell, this.cells[i][j]) <= this.neighborhoodRadius)
					neighbors.push(this.cells[i][j]);
			}

		return neighbors;
	},

	isUnhappy: function(cell) {

		// Count the number of this.cells of different type in the
		// neighborhood.

		var undesirables = 0;

		for(var i = 0; i < cell.n.length; ++i)
			if(cell.n[i].type != cell.type)
				++undesirables;

		// A cell is unhappy if its neighborhood has the same type, to a
		// certain ratio.
		return undesirables / cell.n.length > this.happinessThreshold;
	},

	draw: function() {

		for(var i = 0; i < this.worldSize; ++i) {
			for(var j = 0; j < this.worldSize; ++j) {
				this.ctxt.fillStyle = this.cells[i][j].type == 0 ? 'rgb(76,169,163)' : 'rgb(192,205,102)';
				this.ctxt.fillRect(j * this.cellSize, i * this.cellSize, this.cellSize, this.cellSize);
			}
		}
	}

};
