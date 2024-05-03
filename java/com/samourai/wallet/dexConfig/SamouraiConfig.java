package com.samourai.wallet.dexConfig;

import java.util.Arrays;
import java.util.Collection;

public class SamouraiConfig {
    // extlibj: BackendServer
    private String backendServerMainnetClear = "https://api.samouraiwallet.com/v2";
    private String backendServerMainnetOnion = "http://d2oagweysnavqgcfsfawqwql2rwxend7xxpriq676lzsmtfwbt75qbqd.onion/v2";
    private String backendServerTestnetClear = "https://api.samouraiwallet.com/test/v2";
    private String backendServerTestnetOnion = "http://d2oagweysnavqgcfsfawqwql2rwxend7xxpriq676lzsmtfwbt75qbqd.onion/test/v2";

    // extlibj: SorobanServer
    private String sorobanServerTestnetClear = "https://soroban.samouraiwallet.com/test";
    private String sorobanServerTestnetOnion = "http://sorob4sg7yiopktgz4eom7hl5mcodr6quvhmdpljl5qqhmt6po7oebid.onion/test";
    private String sorobanServerMainnetClear = "https://soroban.samouraiwallet.com";
    private String sorobanServerMainnetOnion = "http://sorob4sg7yiopktgz4eom7hl5mcodr6quvhmdpljl5qqhmt6po7oebid.onion";

    // extlibj: SorobanServerDex
    private Collection<String> sorobanServerDexTestnetClear = Arrays.asList(
            "http://163.172.128.201:4242",
            "http://163.172.130.151:4242",
            "http://163.172.159.127:4242",
            "http://163.172.174.29:4242",
            "http://163.172.159.227:4242",
            "http://51.15.192.136:4242",
            "http://51.158.116.168:4242",
            "http://51.15.226.163:4242",
            "http://212.47.230.157:4242"
    );
    private Collection<String> sorobanServerDexTestnetOnion = Arrays.asList(
            "http://sor2aduqon52pngz56b3pp2niq5vp4l7xstco654gfa37fcaoblle5yd.onion",
            "http://sor3xcg6i4tyt4uawt2t3i3ml4dzubh46wyloptl3g5jyshkgekuumyd.onion",
            "http://sor6jatlc2pim7mg4paxy6kgzuw7qidajlxk7xy6ic6ytcpcy47lucyd.onion",
            "http://sor7qfbf24l3gdba5ed625pfwfebwctiao5po3zux3c6udlboowkucid.onion",
            "http://sorark2anb6q6oz6egxo4zo67cmlnfvjxz2h34v74ta6jozceqclw5yd.onion",
            "http://sorbutari3lpxmqhpzu3jojflteluekxnfyu2jgs4vqkgtsxrjyv4byd.onion",
            "http://sori5763cjjz2iylge5mmgphw7nn2onal7msewk6zoxjwhxhx2crdyad.onion",
            "http://sor4bdycjobng56dusw7hb7oiqykpgblmkaqlg2cdabrh7rkovol3bad.onion",
            "http://sor5fjivjq3rnxloamuux5prbjj5idvca4dulvpkb5bxrneuihrd5wqd.onion"

    );
    private Collection<String> sorobanServerDexMainnetClear = Arrays.asList(
            "http://185.100.84.98:4242",
            "http://185.100.84.130:4242",
            "http://185.100.84.131:4242",
            "http://185.100.84.132:4242",
            "http://185.100.84.133:4242",
            "http://185.100.84.134:4242",
            "http://185.100.84.135:4242",
            "http://185.100.84.136:4242",
            "http://185.100.84.137:4242"
    );
    private Collection<String> sorobanServerDexMainnetOnion = Arrays.asList(
            "http://wmk44xtgyfmc2noamkeoetnc7ufmkqso4apfnnvp4or4mibscumr22id.onion",
            "http://hp4gpnhxkx6u7wzc3fkqf263ni4zfpcta4uxi24bcr2bzfcnu2lw5did.onion",
            "http://ubbd4kioblwsvmpj3hhwvi7vfidzx4why2zui4n5cttuajxxfvzwbkqd.onion",
            "http://6capyi2cfpa7ofpgqb6vippiqw4kwvo7qo3pqzf7xem5uso6uvu7ezyd.onion",
            "http://ayhsx4zxjq4tsk3gkwbppq2u2d6wllfsvubbaro45up53zgmqp2ghcqd.onion",
            "http://idmxhjkai4ptjflenq37vsaz4jvyk5pgg2hpmyjefeflhb23g5yej5yd.onion",
            "http://dncxro3yxldpyznkkf7rxkthh4qf2vw26uzxbfcab34wrgaqve76yyid.onion",
            "http://oxtyspz3im74ebdaxq3ozm2y6jlgiru4m6wn7oznvc6nwpiububel7ad.onion",
            "http://334cb6simoua5uiy4buacqxuh56u3mukeggu3p3xnlmyp7skirv2jzid.onion"
    );

    public SamouraiConfig() {
    }

    public String getBackendServerMainnetClear() {
        return backendServerMainnetClear;
    }

    public void setBackendServerMainnetClear(String backendServerMainnetClear) {
        this.backendServerMainnetClear = backendServerMainnetClear;
    }

    public String getBackendServerMainnetOnion() {
        return backendServerMainnetOnion;
    }

    public void setBackendServerMainnetOnion(String backendServerMainnetOnion) {
        this.backendServerMainnetOnion = backendServerMainnetOnion;
    }

    public String getBackendServerTestnetClear() {
        return backendServerTestnetClear;
    }

    public void setBackendServerTestnetClear(String backendServerTestnetClear) {
        this.backendServerTestnetClear = backendServerTestnetClear;
    }

    public String getBackendServerTestnetOnion() {
        return backendServerTestnetOnion;
    }

    public void setBackendServerTestnetOnion(String backendServerTestnetOnion) {
        this.backendServerTestnetOnion = backendServerTestnetOnion;
    }

    //

    public String getSorobanServerTestnetClear() {
        return sorobanServerTestnetClear;
    }

    public void setSorobanServerTestnetClear(String sorobanServerTestnetClear) {
        this.sorobanServerTestnetClear = sorobanServerTestnetClear;
    }

    public String getSorobanServerTestnetOnion() {
        return sorobanServerTestnetOnion;
    }

    public void setSorobanServerTestnetOnion(String sorobanServerTestnetOnion) {
        this.sorobanServerTestnetOnion = sorobanServerTestnetOnion;
    }

    public String getSorobanServerMainnetClear() {
        return sorobanServerMainnetClear;
    }

    public void setSorobanServerMainnetClear(String sorobanServerMainnetClear) {
        this.sorobanServerMainnetClear = sorobanServerMainnetClear;
    }

    public String getSorobanServerMainnetOnion() {
        return sorobanServerMainnetOnion;
    }

    public void setSorobanServerMainnetOnion(String sorobanServerMainnetOnion) {
        this.sorobanServerMainnetOnion = sorobanServerMainnetOnion;
    }

    //


    public Collection<String> getSorobanServerDexTestnetClear() {
        return sorobanServerDexTestnetClear;
    }

    public void setSorobanServerDexTestnetClear(Collection<String> sorobanServerDexTestnetClear) {
        this.sorobanServerDexTestnetClear = sorobanServerDexTestnetClear;
    }

    public Collection<String> getSorobanServerDexTestnetOnion() {
        return sorobanServerDexTestnetOnion;
    }

    public void setSorobanServerDexTestnetOnion(Collection<String> sorobanServerDexTestnetOnion) {
        this.sorobanServerDexTestnetOnion = sorobanServerDexTestnetOnion;
    }

    public Collection<String> getSorobanServerDexMainnetClear() {
        return sorobanServerDexMainnetClear;
    }

    public void setSorobanServerDexMainnetClear(Collection<String> sorobanServerDexMainnetClear) {
        this.sorobanServerDexMainnetClear = sorobanServerDexMainnetClear;
    }

    public Collection<String> getSorobanServerDexMainnetOnion() {
        return sorobanServerDexMainnetOnion;
    }

    public void setSorobanServerDexMainnetOnion(Collection<String> sorobanServerDexMainnetOnion) {
        this.sorobanServerDexMainnetOnion = sorobanServerDexMainnetOnion;
    }
}
