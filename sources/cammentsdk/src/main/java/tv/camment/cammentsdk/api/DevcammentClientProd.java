package tv.camment.cammentsdk.api;

import com.amazonaws.mobileconnectors.apigateway.annotation.Service;
import com.camment.clientsdk.DevcammentClient;


@Service(
        endpoint = "https://k0ujcx8gu4.execute-api.eu-central-1.amazonaws.com/prod"
)
public interface DevcammentClientProd extends DevcammentClient {
}
