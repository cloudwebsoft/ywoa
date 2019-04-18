<%@ Language=VBScript %>
<%
'This page is meant to output CPU Usage of 2 CPUs
'The data will be picked by FusionWidgets angular gauge. 
'You need to make sure that the output data doesn't contain any HTML tags or carriage returns.

'For the sake of demo, we'll just be generating a random value between 0 and 100 and returning the same.
'In real life applications, you can get the data from web-service or your own data systems, convert it into real-time data format and then return to the chart.

'Set randomize timers on
Randomize()
Randomize Timer

Dim lowerLimit, upperLimit
Dim randomValue1, randomValue2

lowerLimit = 0
upperLimit = 100

'Generate a random value - and round it
randomValue1 = Int(Rnd()*(upperLimit-lowerLimit))+lowerLimit
randomValue2 = Int(Rnd()*(upperLimit-lowerLimit))+lowerLimit

'Now write it to output stream
Response.Write("&value=" & randomValue1 & "|" & randomValue2)

'Or you can also write in the following format - using IDs
'Response.Write("&CPU1=" & randomValue1 & "&CPU2=" & randomValue2)
%>