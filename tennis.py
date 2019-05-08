# -*- coding: utf-8 -*-

from pandas import DataFrame, read_csv
import matplotlib.pyplot as plt
import pandas as pd 

def readFile():
	file = r'predictions.xls'
	df = pd.read_excel(file)

	ax1 = df.plot.scatter(x=2, y=3, color = 'b', label='Winner')
	ax1 = df.plot.scatter(x=0, y=1, color = 'r', label='Loser', ax=ax1)
	ax1.set_xlabel("Rating")
	ax1.set_ylabel("Rating Derivation")
	plt.show()