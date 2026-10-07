package consulo.object.pascal.moduleAware;

import consulo.index.io.data.DataExternalizer;
import consulo.index.io.data.DataInputOutputUtil;
import consulo.index.io.data.IOUtil;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author VISTALL
 * @since 2026-10-07
 */
public class PascalDefineOptionsExternalizer implements DataExternalizer<PascalDefineOptions> {
    public static final PascalDefineOptionsExternalizer INSTANCE = new PascalDefineOptionsExternalizer();

    @Override
    public void save(DataOutput out, PascalDefineOptions value) throws IOException {
        Map<String, String> defines = value.defines();
        DataInputOutputUtil.writeINT(out, defines.size());
        for (Map.Entry<String, String> entry : defines.entrySet()) {
            IOUtil.writeUTF(out, entry.getKey());
            IOUtil.writeUTF(out, entry.getValue());
        }
    }

    @Override
    public PascalDefineOptions read(DataInput in) throws IOException {
        int count = DataInputOutputUtil.readINT(in);
        Map<String, String> defines = new TreeMap<>();
        for (int i = 0; i < count; i++) {
            String name = IOUtil.readUTF(in);
            defines.put(name, IOUtil.readUTF(in));
        }
        return new PascalDefineOptions(defines);
    }
}
