package org.wso2.convertJsonNullValues;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Iterator;

public class ConvertJsonNullValuesClassMediator extends AbstractMediator {

    private JSONObject jsonPayloadObject = null;
    private JSONObject convertedJsonPayloadObject = null;
    private static Log log = LogFactory.getLog(ConvertJsonNullValuesClassMediator.class);

	public boolean mediate(MessageContext context) {

        log.debug("Accessing the JSON conversion class mediator");

		try {

			jsonPayloadObject = new JSONObject(JsonUtil.jsonPayloadToString(((Axis2MessageContext) context)
                    .getAxis2MessageContext()));

            convertedJsonPayloadObject = replaceNullInJSONPayload(jsonPayloadObject);

		} catch (JSONException e) {
			log.error("Error in JSON payload conversion ", e);
		}
		
		JsonUtil.newJsonPayload(
				((Axis2MessageContext) context).getAxis2MessageContext(),
				String.valueOf(convertedJsonPayloadObject), true, true);

		log.debug("Converted json payload " + convertedJsonPayloadObject.toString());

		return true;
	}

	public static JSONObject replaceNullInJSONPayload(JSONObject jsonPayloadObject) {

		try {

			Iterator iterator = jsonPayloadObject.keys();

			while (iterator.hasNext()) {

				Object key = iterator.next();

				String keyString = (String) key;
				Object keyValue = jsonPayloadObject.get(keyString);

				log.debug("KEY: " + keyString + " VALUE: " + keyValue);

				if(keyValue instanceof JSONObject){
                    replaceNullInJSONPayload((JSONObject) keyValue);
                }
                else if(keyValue instanceof JSONArray){
                    for (int i=0;i<((JSONArray)keyValue).length();i++) {
                        replaceNullInJSONPayload(((JSONArray)keyValue).getJSONObject(i));
                    }
                }

				else if (keyValue.toString().equalsIgnoreCase("null")) {
					log.debug("Changing JSON payload Null value inside the class mediator");
					jsonPayloadObject.put(keyString, JSONObject.NULL);
				}
			}

			return jsonPayloadObject;

		} catch (JSONException e1) {
		    log.error("Error in JSON payload conversion ", e1);
			return null;
		}
	}
}
