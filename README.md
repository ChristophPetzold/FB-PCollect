FB-PCollect
===========

Summarize information of a certain Facebook page. Collecting posts and comments of a specified period.
Therefor the collector makes use of the RestFB package (http://restfb.com).

Getting started
===============

In the first place you can change the configuration file (```config.properties```) for your needs. You can find it under ``` /src/de.cpe.fb.pagecollector.custom ```  (see table below).

Once you have set up the configuration file you can run the ```Run.java``` and the output will be generated.


| Property      | Description               | example |note|
| :------------ | :------------------------ |:------- |:---|
| access.appId  | Facbook App Id to be used | ```-``` | 	|
| access.appSecret | Secret token corresponding to the Facebook App Id | ```-``` | |
| config.run.dayEndFormat | specifies end of a day | ```%s +0001 23:59:59``` | has to match config.timestampFormat |
| config.run.dayStartFormat| specifies begin of a day | ```%s +0001 00:00:00``` | has to match config.timestampFormat |
| config.run.input.date.start | **Run parameter:** Start of observed period | ```31.12.2013``` | day before the actual start date |
| config.run.input.date.end | **Run parameter:** End of observed period | ```02.01.2013``` | |
| config.run.input.keywordFile| **Run parameter:** Path to a file conaining keywords. Posts containing such a keyword will be taged as "relevant" | ```path\\to\\keywords.txt```	| optional 									|
| config.run.input.page | **Run parameter:** The Facebook page to be observed | ```GitHub``` | https://www.facebook.com/GitHub |
| config.run.output.file | **Run parameter:** File where the results will be stored. The current implementation writes a xml file. | ```path\\to\\fb_%s.xml``` | |
| config.timeStampFormat | Internal ts format | ```dd.MM.yyyy Z HH:mm:ss``` |  |

