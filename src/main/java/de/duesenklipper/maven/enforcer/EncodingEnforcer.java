package de.duesenklipper.maven.enforcer;

import org.apache.maven.enforcer.rule.api.EnforcerRule;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;

import java.io.File;

public class EncodingEnforcer implements EnforcerRule {
    public void execute(EnforcerRuleHelper helper) throws EnforcerRuleException {
        Log log = helper.getLog();
        log.info("EncodingEnforcer activated");
        try {
            String sourceDirPath = (String) helper.evaluate("${project.source.directory}");
            File sourceDir = new File(sourceDirPath);
        } catch (ExpressionEvaluationException e) {
            throw new EnforcerRuleException("error", e);
        }
    }

    public boolean isCacheable() {
        return false;
    }

    public boolean isResultValid(EnforcerRule enforcerRule) {
        return false;
    }

    public String getCacheId() {
        return "";
    }
}
