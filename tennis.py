# -*- coding: utf-8 -*-

from pandas import DataFrame, read_csv
import matplotlib.pyplot as plt
import pylab
import pandas as pd 
import numpy as np
import csv

from tennisMatchProbability import matchProb

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
		file = r'momentumPredictions.xls'
		df = pd.read_excel(file)

		mom = df['Lower Momentum']
		df['Lower'] = mom.where(df['Predicted'] == 0)
		df['Lower'].hist(bins=[-300, -200, -100, 0, 100, 200, 300])
		plt.xlabel('Rating Momentum of Lower Ranked Player')
		plt.ylabel('Frequency')
		plt.show()

def plot_queue_length(): 
		
	x = [3, 4, 5, 6, 7, 8, 9]
	y1 = [65.55, 65.28, 65.89, 65.89, 65.47, 65.43, 64.90]
	y2 = [64.22, 63.73, 64.04, 64.17, 63.73, 64.35, 64.13]
	y3 = [62.62, 63.28, 64.75, 62.13, 63.11, 61.47, 63.61]
	pylab.plot(x, y1, marker='x', label='2017')
	pylab.plot(x, y2, marker='x', label='2018')
	pylab.plot(x, y3, marker='x', label='2019')
	pylab.xlabel('Number of Momentum Changes Kept in Memory')
	pylab.ylabel('Match Winner Prediction Accuracy')
	pylab.legend(loc='best')
	pylab.show()

def plot_country():
	au = [10.24, 7.87, 4.72, 3.94]
	fo = [16.53, 14.17, 7.87, 11.02]	
	wimb = [7.09, 7.87, 5.51, 1.57]
	uo = [7.09, 9.45, 8.66, 8.66]
	index = ['2015', '2016', '2017', '2018']
	x = [8.24, 9.44, 8.96, 9.64]
	losers = [8.51, 10.37, 9.76, 9.64]
	y = [2015, 2016, 2017, 2018]

	df = pd.DataFrame({'Australian Open': au, 'Roland Garros': fo, 'Wimbledon': wimb,'US Open':uo}, index=index)

	df1 = pd.DataFrame({'ATP Tour': x}, index=index)
	df2 = pd.DataFrame({'Losers that are Native (ATP Tour)': losers}, index=index)

	ax = df1.plot(color='black', linestyle='-', marker='o')
	df2.plot(color='midnightblue', linestyle='-', marker='o', ax=ax)
	df.plot(kind='bar', ax=ax)
	ax.set_xlabel("Year")
	ax.set_ylabel("% of All Wins by Native Player")
	plt.show()

def plot_loser_country():
	fo = [11.02, 12.60, 14.96, 11.81]
	au = [7.87, 7.09, 8.66, 7.09]	
	wimb = [7.09, 7.87, 5.51, 1.57]
	uo = [7.09, 9.45, 8.66, 8.66]
	index = ['2015', '2016', '2017', '2018']
	
	y = [2015, 2016, 2017, 2018]

	df = pd.DataFrame({'Australian Open': au, 'Roland Garros': fo, 'Wimbledon': wimb,'US Open':uo}, index=index)

	df1 = pd.DataFrame({'ATP Tour': x}, index=index)

	ax = df1.plot(color='black', linestyle='-', marker='o')
	df.plot(kind='bar', ax=ax)
	ax.set_xlabel("Year")
	ax.set_ylabel("% of All Wins by Native Player")
	plt.show()

def o_malley():

	f = open('ServeP.csv')
	csv_f = csv.reader(f)
	serve_p = {}

	for row in csv_f:
		if (row[9] != "Service Points Won"):
			serve_p[row[0]] = float(row[9])

	f = open('ReturnP.csv')
	csv_f = csv.reader(f)
	return_p = {}

	for row in csv_f:
		if (row[8] != "Return Pts Won"):
			return_p[row[0]] = float(row[8])	

	f = open('match_data/atp_matches_2017.csv')
	w_f = open('match_data/2018.csv', 'w')
	writer = csv.writer(w_f, delimiter=',', quotechar='"', quoting=csv.QUOTE_MINIMAL)

	csv_f = csv.reader(f)

	count = 0;
	c_count = 0;

	for row in csv_f:
		# if (row[1] != "Us Open"):
		# 	continue

		if ((serve_p.get(row[10]) == None)):
			serve_p[row[10]]=0.601
			return_p[row[10]] = 0.346

		if ((serve_p.get(row[20]) == None)):
			serve_p[row[20]]=0.601
			return_p[row[20]] = 0.346
			

		if ((serve_p.get(row[10]) != None) & (serve_p.get(row[20]) != None) & (row[4] != "D")):
			writer.writerow(row)
			wsp = serve_p[row[10]]
			wrp = return_p[row[10]]

			lsp = serve_p[row[20]]
			lrp = return_p[row[20]]



			lmp = matchProb(lsp, lrp)
			wmp = matchProb(wsp, wrp)	

			if (wmp > lmp):
				c_count = c_count+1
			count = count +1

	print(float(c_count)/count)
	print(c_count)			





	
