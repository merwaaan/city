var ctxt;
var canvasSize;

Math.seedrandom('graphstream!');

var cells = [];

var worldSize = 50;
var cellSize;

var affinities = [
	[1, 0.01, 0],   // LOW
	[0.001, 1.5, 0.01], // MEDIUM
	[0, 0.01, 1.6],   // HIGH
];

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
				type: null,
				next: null,
				age: Math.round(Math.random() * 100)
			};
	}

	// Precompute neighbors.
	for(var i = 0; i < worldSize; ++i)
		for(var j = 0; j < worldSize; ++j)
			cells[i][j].n = neighborhood(cells[i][j]);

	// Initial distribution.

	for(var i = 0; i < worldSize; ++i)
		for(var j = 0; j < worldSize; ++j) {

			var c = cells[i][j];
			var d = Math.sqrt(Math.pow(c.x - worldSize / 2, 2) + Math.pow(c.y - worldSize / 2, 2));

			if(d < 5)
				c.type = 2
			else if(d < 10)
				c.type = 1;
			else
				c.type = 0;
		}

	draw();

	// GO!
	setInterval(function(){
		update();
		draw();
	}, 10);
}

function neighborhood(cell) {

	var neighbors = [];

	// To keep things simple: square neighborhood.

	var neighborhoodRadius = 1;

	var x = cell.x;
	var y = cell.y;

	var minx = x - neighborhoodRadius;
	var maxx = x + neighborhoodRadius;
	var miny = y - neighborhoodRadius;
	var maxy = y + neighborhoodRadius;

	for(var i = 0; i < worldSize; ++i)
		for(var j = 0; j < worldSize; ++j) {

			if(i == cell.y && j == cell.x)
				continue;

			var n = cells[i][j];
			if(n.x >= minx && n.x <= maxx && n.y >= miny && n.y <= maxy)
				neighbors.push(n);
		}

	/*
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
	*/

	return neighbors;
}

var it = 0;

function update() {

	for(var i = 0; i < worldSize; ++i)
		for(var j = 0; j < worldSize; ++j)
			if(ready(cells[i][j]))
				cells[i][j].next = next(cells[i][j]);

	// Switch cell types.
	for(var i = 0; i < worldSize; ++i)
		for(var j = 0; j < worldSize; ++j) {
			if(cells[i][j].next != null) {
				cells[i][j].type = cells[i][j].next;
				cells[i][j].age = 0;
				cells[i][j].next = null;
			}
			else
				cells[i][j].age += 1;
		}

	++it;
}

function ready(cell) {

	var weight = 0.02;
	var offset = 350;

	var r = Math.random();

	var p = 1 / (1 + Math.exp(-weight * (cell.age - offset)));

	return r < p;
}

function next(cell) {

	var potentials = [];
	for(var i = 0; i < 3; ++i)
		potentials.push(potential(cell, i));

	return roulette(potentials);
}

function potential(cell, type) {

	var neighborTypes = [];
	for(var i = 0; i < 3; ++i)
		neighborTypes[i] = countNeighbors(cell, i);

	var potential = 0;
	for(var i = 0; i < 3; ++i)
		potential += neighborTypes[i] * affinities[type][i];

	potential /= cell.n.length;

	return potential;
}

function countNeighbors(cell, type){

	var count = 0;

	for(var i in cell.n)
		if(cell.n[i].type == type)
			++count;

	return count;
}

function roulette(potentials) {

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
}

function draw() {

	// Draw cells.

	for(var i = 0; i < worldSize; ++i)
		for(var j = 0; j < worldSize; ++j) {

			var t = cells[i][j].type;

			if(t == 0)
				color = 'rgb(255, 220, 220)';
			else if(t == 1)
				color = 'rgb(255, 130, 130)';
			else if(t == 2)
				color = 'rgb(255, 50, 50)';
			else
				// If there is green then there's a problem.
				var color = 'green';

			ctxt.fillStyle = color;
			ctxt.fillRect(j * cellSize, i * cellSize, cellSize, cellSize);
		}

	/*
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
	*/
}
