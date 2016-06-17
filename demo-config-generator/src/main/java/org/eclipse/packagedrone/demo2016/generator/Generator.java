package org.eclipse.packagedrone.demo2016.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.eclipse.packagedrone.utils.rpm.build.BuilderContext;
import org.eclipse.packagedrone.utils.rpm.build.FileInformation;
import org.eclipse.packagedrone.utils.rpm.build.RpmBuilder;
import org.eclipse.packagedrone.utils.rpm.build.SimpleFileInformationCustomizer;
import org.eclipse.scada.utils.pkg.deb.DebianPackageWriter;
import org.eclipse.scada.utils.pkg.deb.EntryInformation;
import org.eclipse.scada.utils.pkg.deb.TimestampProvider;
import org.eclipse.scada.utils.pkg.deb.TimestampProvider.SpecificYearTimestampProvider;
import org.eclipse.scada.utils.pkg.deb.control.BinaryPackageControlFile;

public class Generator {

    public void run() throws Exception {
        for (File f : Paths.get(".", "configs").toFile().listFiles()) {
            createDebianPackage(f);
            createRpmPackage(f);
        }
    }

    private void createDebianPackage(File f) throws Exception {
        // properties file doubles for meta infos and content
        Properties properties = new Properties();
        properties.load(new FileInputStream(f));

        // create control file for debian
        final BinaryPackageControlFile controlFile = new BinaryPackageControlFile();

        // just use base name of properties file
        String packageName = f.getName().replace(".properties", "");
        // use version from properties file
        String version = properties.getProperty("version", "0.0.1");

        controlFile.setPackage(packageName);
        controlFile.setArchitecture("all");
        controlFile.setVersion(version);
        controlFile.setMaintainer("Juergen Rose <juergen.rose@ibh-systems.com>");
        controlFile.setDescription("specific configuration for IoT device", "This is an automatically generated configuration package");

        final Path debFile = Paths.get(".", "target", "debs", controlFile.makeFileName());
        debFile.getParent().toFile().mkdirs();

        try (FileOutputStream os = new FileOutputStream(debFile.toFile())) {
            SpecificYearTimestampProvider tsp = new TimestampProvider.SpecificYearTimestampProvider(2016);
            try (final DebianPackageWriter debPkgWriter = new DebianPackageWriter(os, controlFile, tsp)) {
                debPkgWriter.addFile(f, "/usr/local/demo2016/" + f.getName(), new EntryInformation("nobody", "nobody", 0644, false));
            }
        }
    }

    private void createRpmPackage(File f) throws Exception {
        // properties file doubles for meta infos and content
        Properties properties = new Properties();
        properties.load(new FileInputStream(f));

        // just use base name of properties file
        String packageName = f.getName().replace(".properties", "");
        // use version from properties file
        String version = properties.getProperty("version", "0.0.1");

        final Path rpmFile = Paths.get(".", "target", "rpms", packageName + "-" + version + ".rpm");
        rpmFile.getParent().toFile().mkdirs();

        try (RpmBuilder rpmBuilder = new RpmBuilder(packageName, version, "all", rpmFile)) {
            BuilderContext ctx = rpmBuilder.newContext();
            ctx.addDirectory("/usr/local/demo2016/", new SimpleFileInformationCustomizer() {
                @Override
                public void perform(FileInformation information) throws IOException {
                    information.setGroup("nobody");
                    information.setUser("nobody");
                    information.setMode((short) 0755);
                }
            });
            ctx.addFile("/usr/local/demo2016/" + f.getName(), Paths.get(f.getPath()), new SimpleFileInformationCustomizer() {
                @Override
                public void perform(FileInformation information) throws IOException {
                    information.setGroup("nobody");
                    information.setUser("nobody");
                    information.setMode((short) 0644);
                }
            });
            rpmBuilder.build();
        }
    }
}
