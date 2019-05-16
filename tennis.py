# -*- coding: utf-8 -*-

from pandas import DataFrame, read_csv
import matplotlib.pyplot as plt
import pandas as pd 

def readFile():
	file = r'predictions.xls'
	df = pd.read_excel(file)

	r_ratio = df['Lower R'] / df['Higher R'] 
	df['R_ratio'] = r_ratio.where(df['Winner'] == df['Higher'], other=-r_ratio)

	surf_ratio = df['Low Surf R'] / df['High Surf R'] 
	df['Surf_ratio'] = surf_ratio.where(df['Higher Surf'] == df['Winner'], other=-surf_ratio)

	ax1 = df.plot.scatter(x='R_ratio', y='Surf_ratio', c = 'H2H',colormap='viridis')
	label_point(df['R_ratio'], df['Surf_ratio'], df['Loser'], ax1)
	plt.axhline(0)
	plt.axvline(0)


	plt.show()

def label_point(x, y, val, ax):
    a = pd.concat({'x': x, 'y': y, 'val': val}, axis=1)
    for i, point in a.iterrows():
    	if (point['val'] == 'Roger Federer' or point['val'] == 'Rafael Nadal' 
    		or point['val'] == 'Novak Djokovic' ):
        	ax.text(point['x'], point['y'], str(point['val']))	