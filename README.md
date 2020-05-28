This is a simple healthcheck servlet that checks the OSGI state and returns an error state (500) in case services are not registered properly

It can be configured in dxpcloud as follows: 


```
	"readinessProbe": {
		"httpGet": {
			"path": "/o/healthcheck",
			"port": 8080
		},
		"initialDelaySeconds": 120,
		"timeoutSeconds": 5,
		"failureThreshold": 5,
		"successThreshold": 3
	},
	"livenessProbe": {
		"httpGet": {
			"path": "/o/healthcheck",
			"port": 8080
		},
		"initialDelaySeconds": 500,
		"periodSeconds": 60,
		"timeoutSeconds": 5,
		"failureThreshold": 3,
		"successThreshold": 1
	},
```
