# -*- coding: utf-8 -*-

from pandas import DataFrame, read_csv
import matplotlib.pyplot as plt
import pylab
import pandas as pd 
import numpy as np

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

def plot_histogram(): 
		file = r'titles.csv'
		df = pd.read_excel(file)
		df.plot(x='Age', y='Rating', kind='line')
		plt.xticks(np.arange(16, 40, 1.0))
		plt.show()

def plot_sys_constant(): 
		
	x = [0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0, 1.1, 1.2]
	y1 = [66.0069, 66.0069, 66.0069, 66.0069, 66.0069, 66.0069, 66.0069, 66.0069, 66.0069, 66.0069]
	y2 = [64.5232, 64.6174, 64.6174, 64.6617, 64.6617, 64.6617, 64.7059, 64.6617, 64.6617, 64.7059]
	y3 = [62.1148, 62.7869, 62.9508, 62.9508, 63.1148, 63.1148, 63.1148, 63.1148, 63.2787, 63.2787]
	pylab.plot(x, y1, marker='x', label='2017')
	pylab.plot(x, y2, marker='x', label='2018')
	pylab.plot(x, y3, marker='x', label='2019')
	pylab.xlabel('System Constant')
	pylab.ylabel('Match Winner Prediction Accuracy')
	pylab.legend(loc='best')
	pylab.show()

