set terminal png
set output 'degree_distance.png'

set nokey
set xlabel 'Excentration'
set ylabel 'Degré moyen'
set yrange [0:3.5]
set boxwidth 300

plot "degree_distance.dat" using 1:2 with lines
