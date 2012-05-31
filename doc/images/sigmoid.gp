set title 'TITRE'
set xrange [-10:150]
set yrange [-2:2]
set xlabel 't'
set ylabel 'P'
set function style lines

weight = 0.1
offset = 50

f(x) = 1 / (1 + exp(-weight * (x - offset)))
