var ctxt;
var canvasSize;

var cells = [];

var worldSize = 10;
var cellSize;

var neighborhoodRadius = 3;

var affinities = [
	[1, 0.5, 0],
	[0.5, 1, 0.3],
	[0, 0.3, 1],
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
				type: Math.round(Math.random() * 2),
				age: Math.round(Math.random() * 100)
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
	}, 500);
}

function neighborhood(cell) {

	var neighbors = [];

	// To keep things simple: square neighborhood.

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

function update() {

	for(var i = 0; i < worldSize; ++i)
		for(var j = 0; j < worldSize; ++j)
			if(ready(cells[i][j]))
				cells[i][j].next = next(cells[i][j]);

	// Switch cell types.
	for(var i = 0; i < worldSize; ++i)
		for(var j = 0; j < worldSize; ++j)
			if(cells[i][j].next != null) {
				cells[i][j].type = cells[i][j].next;
				cells[i][j].age = 0;
				cells[i][j].next = null;
			}
	else
		++cells[i][j].age;
}

function ready(cell) {

	var weight = 0.1;
	var offset = 50;

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
	console.log('---');
	var neighborTypes = [];
	for(var i = 0; i < 3; ++i){
		console.log('-');
		neighborTypes.push(countNeighbors(cell, i));
	}

	var potential = 0;
	for(var i = 0; i < 3; ++i)
		potential += neighborTypes[i] * affinities[type][i];

	return potential;
}

function countNeighbors(cell, type){

	var count = 0;

	for(var i in cell.n)
		if(cell.n[i].type == type)
			++count;

	if(Math.random() < 0.1)
		console.log(cell.type, type);

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

			var color = null;
			switch(cells[i][j].type) {
			case 0:
				color = 'rgb(255, 220, 220)';
				break;
			case 1:
				color = 'rgb(255, 130, 130)';
				break;
			case 2:
				color = 'rgb(255, 50, 50)';
				break;
			}

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
