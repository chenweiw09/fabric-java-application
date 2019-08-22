package com.my.chen.fabric.sdk;

import lombok.Setter;
import org.bouncycastle.util.encoders.Hex;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;

import java.io.*;
import java.util.Set;

/**
 * @author chenwei
 * @version 1.0
 * @date 2019/8/1
 * @description
 */
@Setter
public class FbUser implements User, Serializable {

    private static final long serialVersionUID = 1L;
    protected String name;
    protected Set<String> roles;
    protected String account;
    protected String affiliation;
    protected Enrollment enrollment;
    protected String mspId;

    private String orgName;

    private transient FbStore fabricStore;
    private String keyForFabricStoreName;
    private String skPath;
    private String certificatePath;


    public FbUser(String leagueName, String orgName, String name, String skPath, String certificatePath, FbStore fabricStore) {
        this.name = name;
        this.orgName = orgName;
        this.fabricStore = fabricStore;
        this.skPath = skPath;
        this.certificatePath = certificatePath;
        this.keyForFabricStoreName = getKeyForFabricStoreName(leagueName, orgName, name);

        String userStr = fabricStore.getValue(keyForFabricStoreName);
        if (null != userStr) {
            saveState();
        } else {
            restoreState();
        }
    }


    public void saveState() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(this);
            oos.flush();
            fabricStore.setValue(keyForFabricStoreName, Hex.toHexString(bos.toByteArray()));
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 这里是恢复当前的用户信息
     */
    private void restoreState() {
        String memberStr = fabricStore.getValue(keyForFabricStoreName);
        if (null != memberStr) {
            // 用户在键值存储中被找到，因此恢复状态
            byte[] serialized = Hex.decode(memberStr);
            ByteArrayInputStream bis = new ByteArrayInputStream(serialized);
            try {
                ObjectInputStream ois = new ObjectInputStream(bis);
                FbUser state = (FbUser) ois.readObject();
                if (state != null) {
                    this.name = state.name;
                    this.roles = state.roles;
                    this.account = state.account;
                    this.affiliation = state.affiliation;
                    this.orgName = state.orgName;
                    this.enrollment = state.enrollment;
                    this.enrollment = state.enrollment;
                    this.mspId = state.mspId;
                }
            } catch (Exception e) {
                throw new RuntimeException(String.format("Could not restore state of member %s", this.name), e);
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Set<String> getRoles() {
        return roles;
    }

    @Override
    public String getAccount() {
        return account;
    }

    @Override
    public String getAffiliation() {
        return affiliation;
    }

    @Override
    public Enrollment getEnrollment() {
        return enrollment;
    }

    @Override
    public String getMspId() {
        return mspId;
    }

    public String getOrgName() {
        return orgName;
    }

    void setEnrollment(Enrollment enrollment) {
        this.enrollment = enrollment;
        saveState();
    }

    void setMspId(String mspID) {
        this.mspId = mspID;
        saveState();
    }

    static String getKeyForFabricStoreName(String leagueName, String orgName, String name) {
        String key = String.format("toKeyValStoreName = user.%s%s%s", leagueName, orgName, name);
        return key;
    }

}
