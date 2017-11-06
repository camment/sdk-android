package tv.camment.cammentsdk.api;

import com.amazonaws.mobileconnectors.apigateway.annotation.Service;
import com.camment.clientsdk.DevcammentClient;


@Service(
        endpoint = "https://61a4mjm5gl.execute-api.eu-central-1.amazonaws.com/dev"
)
public interface DevcammentClientDev extends DevcammentClient {
}
