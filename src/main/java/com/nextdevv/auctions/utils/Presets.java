package com.nextdevv.auctions.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class Presets {
    public static final ItemStack GOLDEN_COINS_HEAD = ClassSerializer.FromStringItemStack(
            """
                    rO0ABXNyABpvcmcuYnVra2l0LnV0aWwuaW8uV3JhcHBlcvJQR+zxEm8FAgABTAADbWFwdAAPTGph
                    dmEvdXRpbC9NYXA7eHBzcgA1Y29tLmdvb2dsZS5jb21tb24uY29sbGVjdC5JbW11dGFibGVNYXAk
                    U2VyaWFsaXplZEZvcm0AAAAAAAAAAAIAAkwABGtleXN0ABJMamF2YS9sYW5nL09iamVjdDtMAAZ2
                    YWx1ZXNxAH4ABHhwdXIAE1tMamF2YS5sYW5nLk9iamVjdDuQzlifEHMpbAIAAHhwAAAABHQAAj09
                    dAABdnQABHR5cGV0AARtZXRhdXEAfgAGAAAABHQAHm9yZy5idWtraXQuaW52ZW50b3J5Lkl0ZW1T
                    dGFja3NyABFqYXZhLmxhbmcuSW50ZWdlchLioKT3gYc4AgABSQAFdmFsdWV4cgAQamF2YS5sYW5n
                    Lk51bWJlcoaslR0LlOCLAgAAeHAAAA0JdAALUExBWUVSX0hFQURzcQB+AABzcQB+AAN1cQB+AAYA
                    AAAEcQB+AAh0AAltZXRhLXR5cGV0AAxkaXNwbGF5LW5hbWV0AAtza3VsbC1vd25lcnVxAH4ABgAA
                    AAR0AAhJdGVtTWV0YXQABVNLVUxMdACUeyJleHRyYSI6W3siYm9sZCI6ZmFsc2UsIml0YWxpYyI6
                    ZmFsc2UsInVuZGVybGluZWQiOmZhbHNlLCJzdHJpa2V0aHJvdWdoIjpmYWxzZSwib2JmdXNjYXRl
                    ZCI6ZmFsc2UsImNvbG9yIjoiYmx1ZSIsInRleHQiOiJQb3Qgb2YgQ29pbnMifV0sInRleHQiOiIi
                    fXNxAH4AAHNxAH4AA3VxAH4ABgAAAANxAH4ACHQACHVuaXF1ZUlkdAAKcHJvcGVydGllc3VxAH4A
                    BgAAAAN0AA1QbGF5ZXJQcm9maWxldAAkMDQwNDljOTAtZDNlOS00NjIxLTljYWYtMDAwMGFhYTYz
                    MDY2c3IAE2phdmEudXRpbC5BcnJheUxpc3R4gdIdmcdhnQMAAUkABHNpemV4cAAAAAF3BAAAAAFz
                    cgAXamF2YS51dGlsLkxpbmtlZEhhc2hNYXA0wE5cEGzA+wIAAVoAC2FjY2Vzc09yZGVyeHIAEWph
                    dmEudXRpbC5IYXNoTWFwBQfawcMWYNEDAAJGAApsb2FkRmFjdG9ySQAJdGhyZXNob2xkeHA/QAAA
                    AAAADHcIAAAAEAAAAAJ0AARuYW1ldAAIdGV4dHVyZXN0AAV2YWx1ZXQAtGV5SjBaWGgwZFhKbGN5
                    STZleUpUUzBsT0lqcDdJblZ5YkNJNkltaDBkSEE2THk5MFpYaDBkWEpsY3k1dGFXNWxZM0poWm5R
                    dWJtVjBMM1JsZUhSMWNtVXZNakF4TmpNM1pETmtaVEV4TkRVeFlUZGtOREk1TWpSaU1ESmlOamcw
                    WW1RMU1EUmxNRE5qWVRSa05ERTJOekJqT1RKaU5UUmpNbUV6TUdNNE9EZG1JbjE5ZlE9PXgAeA==
                    """);
    public static final ItemStack SOME_DUDE = ClassSerializer.FromStringItemStack(
            """
                rO0ABXNyABpvcmcuYnVra2l0LnV0aWwuaW8uV3JhcHBlcvJQR+zxEm8FAgABTAADbWFwdAAPTGph
                dmEvdXRpbC9NYXA7eHBzcgA1Y29tLmdvb2dsZS5jb21tb24uY29sbGVjdC5JbW11dGFibGVNYXAk
                U2VyaWFsaXplZEZvcm0AAAAAAAAAAAIAAkwABGtleXN0ABJMamF2YS9sYW5nL09iamVjdDtMAAZ2
                YWx1ZXNxAH4ABHhwdXIAE1tMamF2YS5sYW5nLk9iamVjdDuQzlifEHMpbAIAAHhwAAAABHQAAj09
                dAABdnQABHR5cGV0AARtZXRhdXEAfgAGAAAABHQAHm9yZy5idWtraXQuaW52ZW50b3J5Lkl0ZW1T
                dGFja3NyABFqYXZhLmxhbmcuSW50ZWdlchLioKT3gYc4AgABSQAFdmFsdWV4cgAQamF2YS5sYW5n
                Lk51bWJlcoaslR0LlOCLAgAAeHAAAA0JdAALUExBWUVSX0hFQURzcQB+AABzcQB+AAN1cQB+AAYA
                AAAEcQB+AAh0AAltZXRhLXR5cGV0AAxkaXNwbGF5LW5hbWV0AAtza3VsbC1vd25lcnVxAH4ABgAA
                AAR0AAhJdGVtTWV0YXQABVNLVUxMdACXeyJleHRyYSI6W3siYm9sZCI6ZmFsc2UsIml0YWxpYyI6
                ZmFsc2UsInVuZGVybGluZWQiOmZhbHNlLCJzdHJpa2V0aHJvdWdoIjpmYWxzZSwib2JmdXNjYXRl
                ZCI6ZmFsc2UsImNvbG9yIjoiYmx1ZSIsInRleHQiOiJUaGUgUG9zdGFsIER1ZGUifV0sInRleHQi
                OiIifXNxAH4AAHNxAH4AA3VxAH4ABgAAAANxAH4ACHQACHVuaXF1ZUlkdAAKcHJvcGVydGllc3Vx
                AH4ABgAAAAN0AA1QbGF5ZXJQcm9maWxldAAkMDQwNDljOTAtZDNlOS00NjIxLTljYWYtMDAwMGFh
                YTU3MzIxc3IAE2phdmEudXRpbC5BcnJheUxpc3R4gdIdmcdhnQMAAUkABHNpemV4cAAAAAF3BAAA
                AAFzcgAXamF2YS51dGlsLkxpbmtlZEhhc2hNYXA0wE5cEGzA+wIAAVoAC2FjY2Vzc09yZGVyeHIA
                EWphdmEudXRpbC5IYXNoTWFwBQfawcMWYNEDAAJGAApsb2FkRmFjdG9ySQAJdGhyZXNob2xkeHA/
                QAAAAAAADHcIAAAAEAAAAAJ0AARuYW1ldAAIdGV4dHVyZXN0AAV2YWx1ZXQAtGV5SjBaWGgwZFhK
                bGN5STZleUpUUzBsT0lqcDdJblZ5YkNJNkltaDBkSEE2THk5MFpYaDBkWEpsY3k1dGFXNWxZM0po
                Wm5RdWJtVjBMM1JsZUhSMWNtVXZOR0psTlRKbFlUUXhOMkUwTmpneU16TXlOMkV3TkRnM09XTmhO
                MkV6TVdNMVlqTTJZelZqTUdVMU1EUmlOekE0TldVMk1HTXdORFV6WlRRNFpHRmxNeUo5ZlgwPXgA
                eA==
                            
                    """);


    public static ItemStack getPlayerHead(String name) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwner(name);
        item.setItemMeta(meta);
        return item;
    }
}
