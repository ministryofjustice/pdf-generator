package uk.gov.justice.digital.pdf.service;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import uk.gov.justice.digital.pdf.Configuration;

import jakarta.inject.Inject;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.Runtime.getRuntime;

@Slf4j
public class HealthService {

    private final Map<String, String> allSettings;

    @Inject
    public HealthService(Configuration configuration) {

        allSettings = configuration.allSettings();
    }

    public Map<String, Object> process() {

        return ImmutableMap.<String, Object>builder()
                .put("status", "OK")
                .put("version", version())
                .put("dateTime", Instant.now().toString())
                .put("runtime", runtimeInfo())
                .put("fileSystems", fileSystemDetails())
                .put("localHost", localhost())
                .put("configuration", allSettings).build();
    }

    private String version() {

        return Optional.ofNullable(HealthService.class.getPackage()).
                flatMap(pkg -> Optional.ofNullable(pkg.getImplementationVersion())).
                orElse("UNKNOWN");
    }

    private Map<String, Object> runtimeInfo() {
        val processors = getRuntime().availableProcessors();
        val systemLoad = ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage() * 100 / processors;

        return ImmutableMap.of(
                "processors", processors,
                "freeMemory", FileUtils.byteCountToDisplaySize(getRuntime().freeMemory()),
                "totalMemory", FileUtils.byteCountToDisplaySize(getRuntime().totalMemory()),
                "maxMemory", FileUtils.byteCountToDisplaySize(getRuntime().maxMemory()),
                "systemLoad", String.format("%.2f %%", systemLoad)
        );
    }

    private String localhost() {

        String localHost;

        try {
            localHost = InetAddress.getLocalHost().toString();

        } catch (UnknownHostException ex) {

            localHost = "unknown";
        }

        return localHost;
    }

    private Iterable<Map<String, String>> fileSystemDetails() {

        return Arrays.stream(File.listRoots()).map(root -> ImmutableMap.of(
                "filePath", root.getAbsolutePath(),
                "totalSpace", FileUtils.byteCountToDisplaySize(root.getTotalSpace()),
                "freeSpace", FileUtils.byteCountToDisplaySize(root.getFreeSpace()),
                "usableSpace", FileUtils.byteCountToDisplaySize(root.getUsableSpace())
        )).collect(Collectors.toList());
    }


}
