set terminal png
set output 'sigmoid.png'

set title 'Sigmoïde'
set xrange [-5:5]
set yrange [-0.5:1.5]
set xlabel 'x'
set ylabel 'f(x)'

f(x) = 1 / (1 + exp(-x))

plot f(x)
