set terminal png
set output 'sigmoid-age.png'

set title 'Probabilité de changement d état en fonction de l age'
set xrange [-10:120]
set yrange [0:1]
set xlabel 't'
set ylabel 'Probabilité'

weight = 0.1
offset = 50
f(x) = 1 / (1 + exp(-weight * (x - offset)))

plot f(x)
