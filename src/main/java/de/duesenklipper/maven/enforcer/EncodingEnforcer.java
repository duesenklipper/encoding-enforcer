package de.duesenklipper.maven.enforcer;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import org.apache.maven.enforcer.rule.api.EnforcerRule;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class EncodingEnforcer implements EnforcerRule {

    private String[] patterns;

    public void execute(EnforcerRuleHelper helper) throws EnforcerRuleException {
        final Log log = helper.getLog();
        log.info("EncodingEnforcer activated");
        try {
            Path baseDir = ((File) helper.evaluate("${pom.basedir}")).toPath().toAbsolutePath();
            String outputDirPath = (String) helper.evaluate("${project.build.directory}");
            Path outputDir = Paths.get(outputDirPath);
            if (patterns == null || patterns.length == 0) {
                throw new EnforcerRuleException("no patterns configured");
            }
            log.info(baseDir.toString());
            StringBuilder result = new StringBuilder();
            for (String pattern : patterns) {
                String[] strings = pattern.split(":");
                String glob = strings[0];
                String encoding = strings[1];
                log.info(glob);
                log.info(encoding);
                PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + glob);
                Files.walkFileTree(baseDir, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        log.info("visiting: " + file.toString());
                        if (file.startsWith(outputDir)) {
                            log.info("skipping");
                            return FileVisitResult.SKIP_SUBTREE;
                        } else if (pathMatcher.matches(file)) {
                            log.info("match");
                            try (InputStream in = new BufferedInputStream(new FileInputStream(file.toFile()))) {
                                CharsetMatch[] charsetMatches = new CharsetDetector().setText(in).detectAll();
                                boolean found = false;
                                for (CharsetMatch charsetMatch : charsetMatches) {
                                    if (encoding.equals(charsetMatch.getName())) {
                                        log.info("found " + charsetMatch.getName());
                                        found = true;
                                        break;
                                    }
                                }
                                if (!found) {
                                    log.info("not found");
                                    result.append(file.toString());
                                    result.append(": Required encoding ").append(encoding).append(" not confirmed. ");
                                    result.append("Detected encoding: ").append(charsetMatches[0].getName());
                                    result.append("\n");
                                }
                            }
                        } else {
                            log.info("no match");
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
            if (result.length() > 0) {
                log.info("throwing rule exception");
                throw new EnforcerRuleException(result.toString());
            }
        } catch (ExpressionEvaluationException e) {
            throw new EnforcerRuleException("error", e);
        } catch (IOException e) {
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
