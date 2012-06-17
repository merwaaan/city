set terminal png
set output 'degree.png'

set nokey
set xlabel 'Temps'
set ylabel 'Degré moyen des carrefours'

plot "degree.dat" with lines
