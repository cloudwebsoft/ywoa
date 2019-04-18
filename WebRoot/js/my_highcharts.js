function lineCharts(id,params){
	  $(id).highcharts({
	        title: {
	            text: params.title,
	            x: -20 //center
	        },
	        subtitle: {
	            text:params.subtitle,
	            x: -20
	        },
	        xAxis: {
	            categories: params.xCategories
	        },
	        yAxis: {
	        	min:0,
	            title: {
	                text: params.yTitle
	            },
	            plotLines: [{
	                value: 0,
	                width: 1,
	                color: '#808080'
	            }]
	        },
	        tooltip: {
	            valueSuffix: params.unit
	        },
	        legend: {
	            layout: 'vertical',
	            align: 'right',
	            verticalAlign: 'middle',
	            borderWidth: 0
	        },
	        series: params.data
	    });

	
	
	
	
}

function pieCharts(id,params){
	 $(id).highcharts({
	        chart: {
	            type: 'pie',
	            options3d: {
	                enabled: true,
	                alpha: 45,
	                beta: 0
	            }
	        },
	        title: {
	            text:params.title
	        },
	        tooltip: {
	            pointFormat: params.tooltip
	        },
	        plotOptions: {
	            pie: {
	                allowPointSelect: true,
	                cursor: 'pointer',
	                depth: 35,
	                dataLabels: {
	                    enabled: true,
	                    format:params.plotOptions
	                }
	            }
	        },
	        series: [{
	            type: 'pie',
	            name: params.seriesName,
	            data: params.data
	        }]
	    });
}

function funnelChart(id,params){
	 $(id).highcharts({
	        chart: {
	            type: 'funnel',
	            marginRight: 100
	        },
	        title: {
	            text:params.title,
	            x: -50
	        },
	        plotOptions: {
	            series: {
	                dataLabels: {
	                    enabled: true,
	                    format: '<b>{point.name}</b> ({point.y:,.0f})',
	                    color: 'black',
	                    softConnector: true
	                },
	                neckWidth: '30%',
	                neckHeight: '25%'
	                
	                //-- Other available options
	                // height: pixels or percent
	                // width: pixels or percent
	            }
	        },
	        legend: {
	            enabled: false
	        },
	        series: [{
	            name: params.seriesName,
	            data: params.data
	        }]
	    });

	
	
	
	
	
}
