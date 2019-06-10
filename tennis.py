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
		
	x = [0.04, 0.045, 0.05, 0.055, 0.06, 0.065, 0.07, 0.075, 0.08]
	y1 = [65.012, 65.40, 65.70,65.06,65.55,65.40,65.25,64.94,65.66]
	y2 = [64.00,64.09,63.91,64.26,64.57,64.40,64.31,64.66,64.61]
	y3 = [62.79,63.77,63.44,63.11,63.61,63.61,62.30,63.11,62.46]
	pylab.plot(x, y1, marker='x', label='2017')
	pylab.plot(x, y2, marker='x', label='2018')
	pylab.plot(x, y3, marker='x', label='2019')
	pylab.xlabel('Volatility Value')
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

def plot_h2h():
	first = [63.95, 63.78]
	second = [64.66, 63.20]	
	third = [66.16, 66.31]
	fourth = [65.70, 65.47]
	z = ['With No of titles', 'W/O No of Titles']

	df = pd.DataFrame({'1990-2018': first, '2015-2018': second, '1990-2017': third,'2015-2017':fourth}, index=z)

	ax = df.plot(linestyle='-', marker='o')
	ax.set_xlabel("ML Model Attributes")
	ax.set_ylabel("Succes %")
	plt.xticks(range(len(df.index)), df.index)
	plt.show()

def plot_avg_age():
	x = [17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39]	
	y = [1373, 1451, 1473, 1525, 1533, 1514, 1497, 1505, 1539, 1512, 1548, 1614, 1634, 1572, 1569, 1522, 1544, 1525, 1539, 1608, 1551, 1458, 1361]

	pylab.plot(x, y, marker='x')
	pylab.xlabel('Age')
	pylab.ylabel('Average Rating')
	pylab.show()

def plot_bars():
	# data to plot
	n_groups = 4
	general = (62.59, 62.14, 62.27, 55.55)
	specific = (61.57, 61.87, 61.48, 58.25)
	mix = (62.63, 62.23, 62.25, 59.75)

	# create plot
	fig, ax = plt.subplots()
	index = np.arange(n_groups)
	bar_width = 0.20
	opacity = 0.8

	rects1 = plt.bar(index, general, bar_width,
	alpha=opacity,
	color='b',
	label='General Ratings')

	rects2 = plt.bar(index + bar_width, specific, bar_width,
	alpha=opacity,
	color='g',
	label='Surface Specific Ratings')

	rects3 = plt.bar(index + bar_width + bar_width, mix, bar_width,
	alpha=opacity,
	color='r',
	label='Both Ratings')

	plt.xlabel('Classifier Model')
	plt.ylabel('Prediction Succes %')
	plt.title('2018 Matches')
	plt.xticks(index + bar_width, ('Logistic Regression', 'Decision Tree',
	 'SVM', 'Random Forest'))
	plt.legend()

	plt.tight_layout()
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

	f = open('match_data/atp_matches_2018.csv')
	w_f = open('match_data/2018.csv', 'w')
	writer = csv.writer(w_f, delimiter=',', quotechar='"', quoting=csv.QUOTE_MINIMAL)

	csv_f = csv.reader(f)

	count = 0;
	c_count = 0;

	for row in csv_f:
		if (row[1] != "Us Open"):
			continue

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
			count = count + 1

	print(float(c_count)/count)
	print(count)
	print(c_count)			





	
