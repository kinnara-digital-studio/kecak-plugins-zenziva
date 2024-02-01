package com.kinnarastudio.kecakplugins.zenziva.tool;

import com.kinnarastudio.zenzivanet.model.ApiAccount;
import com.kinnarastudio.zenzivanet.model.ZenzivaClient;
import com.kinnarastudio.zenzivanet.model.exception.RequestException;
import com.kinnarastudio.zenzivanet.model.exception.ResponseException;
import com.kinnarastudio.zenzivanet.model.response.RegularSmsResponse;
import com.kinnarastudio.zenzivanet.model.response.RegularWhatsappResponse;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.DefaultApplicationPlugin;
import org.joget.plugin.base.PluginManager;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 */
public class ZenzivaNotificationTool extends DefaultApplicationPlugin {
    public final static String LABEL = "Zenziva Notification Tool";

    @Override
    public String getName() {
        return LABEL;
    }

    @Override
    public String getVersion() {
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        ResourceBundle resourceBundle = pluginManager.getPluginMessageBundle(getClassName(), "/messages/BuildNumber");
        String buildNumber = resourceBundle.getString("buildNumber");
        return buildNumber;
    }

    @Override
    public String getDescription() {
        return getClass().getPackage().getImplementationTitle();
    }

    @Override
    public Object execute(Map properties) {
        final String userKey = getUserKey();
        final String apiKey = getApiKey();

        final ZenzivaClient client = new ZenzivaClient.Builder()
                .setAccount(new ApiAccount(userKey, apiKey))
                .build();


        final String toPhone = getToPhone();
        final String message = getMessage();

        try {
            final Set<String> messageType = getMessageType();

            // regular SMS
            if (messageType.contains("regularSms")) {
                final RegularSmsResponse response = client.executePostRegularSms(toPhone, message);
                LogUtil.info(getClass().getName(), "RegularSmsResponse : to [" + response.getTo() + "] status [" + response.getStatus() + "] messageId [" + response.getMessageId() + "] response [" + response.getText() + "]");
            }

            // regular Whatsapp
            if (messageType.contains("regularWhatsapp")) {
                final RegularWhatsappResponse response = client.executePostRegularWhatsapp(toPhone, message);
                LogUtil.info(getClass().getName(), "RegularWhatsappResponse : to [" + response.getTo() + "] status [" + response.getStatus() + "] messageId [" + response.getMessageId() + "] response [" + response.getText() + "]");
            }

        } catch (ResponseException | RequestException e) {
            LogUtil.error(getClass().getName(), e, e.getMessage());
        }
        return null;
    }

    @Override
    public String getLabel() {
        return LABEL;
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/tool/ZenzivaNotificationTool.json", null, true, "/messages/ZenzivaNotificationTool");
    }

    protected String getUserKey() {
        return getPropertyString("userKey");
    }

    protected String getApiKey() {
        return getPropertyString("apiKey");
    }

    protected Set<String> getMessageType() {
        return Optional.of(getPropertyString("messageType"))
                .map(s -> s.split(";"))
                .map(Arrays::stream)
                .orElseGet(Stream::empty)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    protected String getToPhone() {
        return getPropertyString("toPhone");
    }

    protected String getMessage() {
        return getPropertyString("message");
    }
}
