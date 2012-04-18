var ctxt;
var canvasSize;

var cells = [];

var worldSize = 200;
var cellSize;

window.onload = function() {

	ctxt = document.querySelector('canvas').getContext('2d');
	canvasSize = ctxt.canvas.width;
	cellSize = canvasSize / worldSize;

	// Initialize cells.
	for(var i = 0; i < worldSize; ++i) {

		cells[i] = [];

		for(var j = 0; j < worldSize; ++j){

				var type = Math.random() < 0.5 ? 0 : 1;

				cells[i][j] = {
					x: j,
					y: i,
					type: type,
					next: type
				};
			}
		}

	// Precompute neighbors.
	for(var i = 0; i < worldSize; ++i)
		for(var j = 0; j < worldSize; ++j)
			cells[i][j].n = neighborhood(cells[i][j]);

	// GO!
	setInterval(function(){
		update();
		draw();
	}, 100);
}

function update() {

	// Compute next state.
	for(var i = 0; i < worldSize; ++i)
		for(var j = 0; j < worldSize; ++j) {

			var cell = cells[i][j];
			var neighbors = countAliveNeighbors(cell);

			if(cell.type === 0 && neighbors == 3)
				cell.next = 1;
			else if(cell.type === 1 && (neighbors < 2 || neighbors > 3))
				cell.next = 0;
		}

	// Switch cell states.
	for(var i = 0; i < worldSize; ++i)
		for(var j = 0; j < worldSize; ++j)
			cells[i][j].type = cells[i][j].next;
}

function countAliveNeighbors(cell) {

	var count = 0;

	for(var i = 0; i < cell.n.length; ++i)
		if(cell.n[i].type === 1)
			++count;

	return count;
}

function neighborhood(cell) {

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

			var nx = (worldSize + x + j) % worldSize;
			var ny = (worldSize + y + i) % worldSize;
			var n = cells[ny][nx];
			neighbors.push(n);
		}

	return neighbors;
}

function draw() {

	for(var i = 0; i < worldSize; ++i) {
		for(var j = 0; j < worldSize; ++j) {
			ctxt.fillStyle = cells[i][j].type == 0 ? 'white' : 'rgb(192,205,102)';
			ctxt.fillRect(j * cellSize, i * cellSize, cellSize, cellSize);
		}
	}
}
