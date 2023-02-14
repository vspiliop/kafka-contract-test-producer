import org.springframework.cloud.contract.spec.Contract

Contract.make {
	ignored()
	label("testUpdatedEvent")
	input {
		triggeredBy("sendTestUpdatedEvent()")
	}
	outputMessage {
		sentTo("topic1")
		body(
			  "type": null,
			  "correlationId": value(consumer('5d1f9fef-e0dc-4f3d-a7e4-72d2220dd827'),
					  producer(regex(uuid()))
			  ),
				"payload": [
					"io.github.vspiliop.schema.test.TestUpdated": [
						"text": "this is a test updated event"
					]
				]
		)
// -----------------------
// | non dynamic version |
// -----------------------
//
//		body('''
//			{
//			  "type": "TestUpdated",
//			  "correlationId": "b1ada841-a5fe-4363-92f5-2bccfd06db55",
//			  "payload": {
//				"io.github.vspiliop.schema.test.TestUpdated": {
//				  "text": "this is a test created event"
//				}
//			  }
//			}
//		''')
	}
}