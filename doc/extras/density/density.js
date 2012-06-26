function Density(canvasId, worldSize, stabilized) {

	this.canvas = document.querySelector('#' + canvasId);
	this.ctxt = this.canvas.getContext('2d');
	this.canvasSize = this.ctxt.canvas.width;

	this.worldSize = worldSize;
	this.cellSize = this.canvasSize /this.worldSize;
	this.cells = [];

	this.stabilized = stabilized;

	this.affinities = [
		[1, 0.01, 0],   // LOW
		[0.001, 1.5, 0.01], // MEDIUM
		[0, 0.01, 1.6],   // HIGH
	];

	// Initialize this.cells.
	for(var i = 0; i < this.worldSize; ++i) {

		this.cells[i] = [];

		for(var j = 0; j < this.worldSize; ++j)
			this.cells[i][j] = {
				x: j,
				y: i,
				type: null,
				next: null,
				age: Math.round(Math.random() * 100)
			};
	}

	// Precompute neighbors.
	for(var i = 0; i < this.worldSize; ++i)
		for(var j = 0; j < this.worldSize; ++j)
			this.cells[i][j].n = this.neighborhood(this.cells[i][j]);

	// Initial density distribution.
	for(var i = 0; i < this.worldSize; ++i)
		for(var j = 0; j < this.worldSize; ++j) {

			var c = this.cells[i][j];
			var d = Math.sqrt(Math.pow(c.x - this.worldSize / 2, 2) + Math.pow(c.y - this.worldSize / 2, 2));

			if(d < 5)
				c.type = 2
			else if(d < 10)
				c.type = 1;
			else
				c.type = 0;
		}

	this.draw();

	this.playing = false;

	// GO!
	var t = this;
	this.listener = this.canvas.addEventListener('click', function(){ t.toggle(); }, false);

}

Density.prototype ={

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

		for(var i = 0; i < this.worldSize; ++i)
			for(var j = 0; j < this.worldSize; ++j)
				if(!this.stabilized || this.ready(this.cells[i][j]))
					this.cells[i][j].next = this.next(this.cells[i][j]);

		// Switch cell types.
		for(var i = 0; i < this.worldSize; ++i)
			for(var j = 0; j < this.worldSize; ++j) {
				if(this.cells[i][j].next != null) {
					this.cells[i][j].type = this.cells[i][j].next;
					this.cells[i][j].age = 0;
					this.cells[i][j].next = null;
				}
				else
					this.cells[i][j].age += 1;
			}
	},

	ready: function(cell) {

		var weight = 0.02;
		var offset = 350;

		var r = Math.random();

		var p = 1 / (1 + Math.exp(-weight * (cell.age - offset)));

		return r < p;
	},

	next: function(cell) {

		var potentials = [];
		for(var i = 0; i < 3; ++i)
			potentials.push(this.potential(cell, i));

		return this.roulette(potentials);
	},

	potential: function(cell, type) {

		var neighborTypes = [];
		for(var i = 0; i < 3; ++i)
			neighborTypes[i] = this.countNeighbors(cell, i);

		var potential = 0;
		for(var i = 0; i < 3; ++i)
			potential += neighborTypes[i] * this.affinities[type][i];

		potential /= cell.n.length;

		return potential;
	},

	countNeighbors: function(cell, type){

		var count = 0;

		for(var i in cell.n)
			if(cell.n[i].type == type)
				++count;

		return count;
	},

	roulette: function(potentials) {

		var wheel = [
			potentials[0],
			potentials[1],
			potentials[2],
		];

		var total = 0;
		for(var i in wheel)
			total += wheel[i];

		var x = Math.random() * total;

		var acc = 0;
		for(var i in wheel) {
			acc += wheel[i];
			if(x <= acc)
				return i;
		}

		return null;
	},

	neighborhood: function(cell) {

		var neighbors = [];

		// To keep things simple: square neighborhood.

		var neighborhoodRadius = 1;

		var x = cell.x;
		var y = cell.y;

		var minx = x - neighborhoodRadius;
		var maxx = x + neighborhoodRadius;
		var miny = y - neighborhoodRadius;
		var maxy = y + neighborhoodRadius;

		for(var i = 0; i < this.worldSize; ++i)
			for(var j = 0; j < this.worldSize; ++j) {

				if(i == cell.y && j == cell.x)
					continue;

				var n = this.cells[i][j];
				if(n.x >= minx && n.x <= maxx && n.y >= miny && n.y <= maxy)
					neighbors.push(n);
			}

		return neighbors;
	},


	draw: function() {

		for(var i = 0; i < this.worldSize; ++i)
			for(var j = 0; j < this.worldSize; ++j) {

				var t = this.cells[i][j].type;

				if(t == 0)
					color = 'rgb(255, 220, 220)';
				else if(t == 1)
					color = 'rgb(255, 130, 130)';
				else if(t == 2)
					color = 'rgb(255, 50, 50)';

				this.ctxt.fillStyle = color;
				this.ctxt.fillRect(j * this.cellSize, i * this.cellSize, this.cellSize, this.cellSize);
			}
	}

}
