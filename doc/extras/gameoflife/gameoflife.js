function GameOfLife(canvasId, worldSize) {

	this.canvas = document.querySelector('#' + canvasId);
	this.ctxt = this.canvas.getContext('2d');
	this.canvasSize = this.ctxt.canvas.width;

	this.worldSize = worldSize;
	this.cellSize = this.canvasSize /this.worldSize;
	this.cells = [];

	// Initializethis.cells.
	for(var i = 0; i <this.worldSize; ++i) {

		this.cells[i] = [];

		for(var j = 0; j < this.worldSize; ++j){

			var type = Math.random() < 0.5 ? 0 : 1;

			this.cells[i][j] = {
				x: j,
				y: i,
				type: type,
				next: type
			};
		}
	}

	// Precompute neighbors.
	for(var i = 0; i <this.worldSize; ++i)
		for(var j = 0; j <this.worldSize; ++j)
			this.cells[i][j].n = this.neighborhood(this.cells[i][j]);

	this.draw();

	this.playing = false;

	// GO!
	var t = this;
	this.listener = this.canvas.addEventListener('click', function(){ t.toggle(); }, false);

};

GameOfLife.prototype = {

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

		// Compute next state.
		for(var i = 0; i < this.worldSize; ++i)
			for(var j = 0; j < this.worldSize; ++j) {

				var cell = this.cells[i][j];
				var neighbors = this.countAliveNeighbors(cell);

				if(cell.type === 0 && neighbors == 3)
					cell.next = 1;
				else if(cell.type === 1 && (neighbors < 2 || neighbors > 3))
					cell.next = 0;
			}

		// Switch cell states.
		for(var i = 0; i < this.worldSize; ++i)
			for(var j = 0; j < this.worldSize; ++j)
				this.cells[i][j].type = this.cells[i][j].next;
	},

	countAliveNeighbors: function(cell) {

		var count = 0;

		for(var i = 0; i < cell.n.length; ++i)
			if(cell.n[i].type === 1)
				++count;

		return count;
	},

	neighborhood: function(cell) {

		// Compute Moore neighborhood.

		var neighbors = [];

		var x = cell.x;
		var y = cell.y;

		var minx = x - 1;
		var maxx = x + 1;
		var miny = y - 1;
		var maxy = y + 1;

		for(var i = -1; i < 2; ++i)
			for(var j = -1; j < 2; ++j){

				if(i == 0 && j == 0)
					continue;

				var nx = (this.worldSize + x + j) % this.worldSize;
				var ny = (this.worldSize + y + i) % this.worldSize;
				var n = this.cells[ny][nx];
				neighbors.push(n);
			}

		return neighbors;
	},

	draw: function() {

		for(var i = 0; i < this.worldSize; ++i)
			for(var j = 0; j < this.worldSize; ++j) {
				this.ctxt.fillStyle = this.cells[i][j].type == 0 ? 'white' : 'rgb(192,205,102)';
				this.ctxt.fillRect(j * this.cellSize, i * this.cellSize, this.cellSize, this.cellSize);
			}
	}
};
