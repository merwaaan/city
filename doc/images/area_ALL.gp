set terminal png
set output 'area_ALL.png'

set xlabel 'Temps'
set ylabel 'Superficie'

plot "area_LOW.dat" with lines title "densité faible",\
     "area_MEDIUM.dat" with lines title "densité moyenne",\
     "area_HIGH.dat" with lines title "densité élevée"
