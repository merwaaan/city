var ctxt;
var canvasSize;

var cells = [];

var neighborhoodRadius = 3;
var happinessThreshold = 0.5;

var worldSize = 100;
var cellSize;

window.onload = function() {

	ctxt = document.querySelector('canvas').getContext('2d');
	canvasSize = ctxt.canvas.width;
	cellSize = canvasSize / worldSize;

	// Initialize cells.
	for(var i = 0; i < worldSize; ++i) {

		cells[i] = [];

		for(var j = 0; j < worldSize; ++j)
			cells[i][j] = {
				x: j,
				y: i,
				type: Math.random() < 0.5 ? 0 : 1
			};
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

function neighborhood(cell) {

	var neighbors = [];

	/*
	// To keep things simple: square neighborhood.

	var x = cell.x;
	var y = cell.y;

	var minx = x - neighborhoodRadius;
	var maxx = x + neighborhoodRadius;
	var miny = y - neighborhoodRadius;
	var maxy = y + neighborhoodRadius;

	for(var i = 0; i < worldSize; ++i)
	for(var j = 0; j < worldSize; ++j){

	if(i == cell.y && j == cell.x)
	continue;

	var n = cells[i][j];
	if(n.x >= minx && n.x <= maxx && n.y >= miny && n.y <= maxy)
	neighbors.push(n);
	}
	*/

	// Radial neighborhood.

	var distance = function(c1, c2) {
		return Math.sqrt(Math.pow(c1.x - c2.x, 2) + Math.pow(c1.y - c2.y, 2))
	}

	for(var i = 0; i < worldSize; ++i)
		for(var j = 0; j < worldSize; ++j){

			if(i == cell.y && j == cell.x)
				continue;

			if(distance(cell, cells[i][j]) <= neighborhoodRadius)
				neighbors.push(cells[i][j]);
		}

	return neighbors;
}

function update() {

	var unhappy = [];

	// Check for unhappy cells.
	for(var i = 0; i < worldSize; ++i)
		for(var j = 0; j < worldSize; ++j)
			if(isUnhappy(cells[i][j]))
				unhappy.push(cells[i][j]);

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
}

function isUnhappy(cell) {

	// Count the number of cells of different type in the
	// neighborhood.

	var undesirables = 0;

	for(var i = 0; i < cell.n.length; ++i)
		if(cell.n[i].type != cell.type)
			++undesirables;

	// A cell is unhappy if its neighborhood has the same type, to a
	// certain ratio.
	return undesirables / cell.n.length > happinessThreshold;
}

function draw() {

	// Draw cells.

	for(var i = 0; i < worldSize; ++i) {
		for(var j = 0; j < worldSize; ++j) {
			ctxt.fillStyle = cells[i][j].type == 0 ? 'rgb(76,169,163)' : 'rgb(192,205,102)';
			ctxt.fillRect(j * cellSize, i * cellSize, cellSize, cellSize);
		}
	}

	// Draw white lines between cells.

	ctxt.strokeStyle = 'white'
	for(var i = 0; i < worldSize; ++i) {
		ctxt.beginPath();
		ctxt.moveTo(0, i * cellSize);
		ctxt.lineTo(canvasSize, i * cellSize);
		ctxt.stroke();
	}
	for(var i = 0; i < worldSize; ++i) {
		ctxt.beginPath();
		ctxt.moveTo(i * cellSize, 0);
		ctxt.lineTo(i * cellSize, canvasSize);
		ctxt.stroke();
	}

}
