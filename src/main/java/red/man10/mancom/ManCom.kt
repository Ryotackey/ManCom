package red.man10.mancom

import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import red.man10.kotlin.CustomConfig
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.block.BlockFace
import org.bukkit.event.block.BlockRedstoneEvent
import org.bukkit.material.Openable
import java.lang.ClassCastException

class ManCom : JavaPlugin(), Listener {

    var config: CustomConfig? = null

    var keyhash: HashMap<String, MutableList<ItemStack>> = HashMap()
    var namehash: HashMap<String, String> = HashMap()
    var drophash: HashMap<String, Boolean> = HashMap()

    val prefix = "§f§l[§b§lMan10Security§f§l]"

    override fun onEnable() {
        // Plugin startup logic

        getCommand("ms").executor = this

        server.pluginManager.registerEvents(this, this)

        config = CustomConfig(this)
        config!!.saveDefaultConfig()

        for (key in config!!.getConfig()!!.getConfigurationSection("keyitem").getKeys(false)) {
            keyhash[key] = config!!.getConfig()!!.getList("keyitem.$key") as MutableList<ItemStack>
        }

        for (key in config!!.getConfig()!!.getConfigurationSection("name").getKeys(false)) {
            namehash[key] = config!!.getConfig()!!.getString("name.$key")
        }

        for (key in config!!.getConfig()!!.getConfigurationSection("drop").getKeys(false)) {
            drophash[key] = config!!.getConfig()!!.getBoolean("drop.$key")
        }

    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    override fun onCommand(sender: CommandSender?, command: Command?, label: String?, args: Array<out String>?): Boolean {
        if (sender !is Player)return true
        val p: Player = sender

        if (!p.hasPermission("mancom.op")){
            return true
        }

        if (args != null) {
            when(args.size){

                0 ->{

                    p.sendMessage("§e§l-------§f§l[§b§lMan10Security§f§l]§e§l-------")
                    sender.sendMessage("§6§l/ms create [名前]§f§r:地点登録。持ってるものが最初のキーになる。")
                    sender.sendMessage("§6§l/ms list§f§r:登録されてる地点の一覧が見れる。")
                    sender.sendMessage("§6§l/ms keyadd [名前]§f§r:[名前]の地点に持ってるアイテムをキーとして追加できる")
                    sender.sendMessage("§6§l/ms keyremove [名前] [番号]§f§r:[名前]の地点の[番号]のカギを消せる")
                    sender.sendMessage("§6§l/ms keylist [名前]§f§r:[名前]の地点のカギ一覧が見れる")
                    sender.sendMessage("§6§l/ms dropset [名前] [true/false]§f§r:[名前]の地点のカギを消費するかどうかを変えられる")
                    sender.sendMessage("§6§l/ms remove [名前]§f§r:[名前]の地点を消せる")
                    sender.sendMessage("§b§lcreated by Ryotackey")
                    sender.sendMessage("§e§l------------------------------")

                    return true
                }

                1 ->{

                    if(args!![0].equals("help", ignoreCase = true)){

                        p.sendMessage("§e§l-------§f§l[§b§lMan10_Security§f§l]§e§l-------")
                        sender.sendMessage("§6§l/ms create [名前]§f§r:地点登録。持ってるものが最初のキーになる。")
                        sender.sendMessage("§6§l/ms list§f§r:登録されてる地点の一覧が見れる。")
                        sender.sendMessage("§6§l/ms keyadd [名前]§f§r:[名前]の地点に持ってるアイテムをキーとして追加できる")
                        sender.sendMessage("§6§l/ms keyremove [名前] [番号]§f§r:[名前]の地点の[番号]のカギを消せる")
                        sender.sendMessage("§6§l/ms keylist [名前]§f§r:[名前]の地点のカギ一覧が見れる")
                        sender.sendMessage("§6§l/ms dropset [名前] [true/false]§f§r:[名前]の地点のカギを消費するかどうかを変えられる")
                        sender.sendMessage("§6§l/ms remove [名前]§f§r:[名前]の地点を消せる")
                        sender.sendMessage("§b§lcreated by Ryotackey")
                        sender.sendMessage("§e§l------------------------------")

                        return true

                    }

                    if (args!![0].equals("list", ignoreCase = true)){

                        var i = 0

                        for (entry in namehash.entries){

                            if (i == 0)p.sendMessage("$prefix§a§l登録された地点一覧")

                            val drop = config!!.getConfig()!!.getBoolean("drop." + entry.value)

                            p.sendMessage("§b$i§f: ${entry.value} / ${entry.key} / $drop")

                            i += 1
                        }

                        return true

                    }

                }

                2 ->{

                    if (args!![0].equals("create", ignoreCase = true)){

                        if (namehash.containsValue(args[1])){
                            p.sendMessage("$prefix§c名前がかぶってます")
                            return true
                        }

                        val itemlist = mutableListOf<ItemStack>()

                        itemlist.add(p.inventory.itemInMainHand)

                        val location = p.location.block.location

                        namehash["${location.blockX}/" + location.blockY + "/" + location.blockZ + "/" + location.world.name] = args[1]
                        keyhash[args[1]] = itemlist
                        drophash[args[1]] = false

                        for (entry in namehash.entries){
                            config!!.getConfig()!!.set("name." + entry.key, entry.value)
                        }

                        for (entry in keyhash.entries){
                            config!!.getConfig()!!.set("keyitem." + entry.key, entry.value)
                        }

                        for (entry in drophash.entries){
                            config!!.getConfig()!!.set("drop." + entry.key, entry.value)
                        }

                        config!!.saveConfig()

                        p.sendMessage("$prefix§e" + args[1] + "§aを登録しました")

                        return true

                    }

                    if (args!![0].equals("keylist", ignoreCase = true)){

                        if (!namehash.containsValue(args[1])){
                            p.sendMessage("$prefix§cその名前は登録されていません")
                            return true
                        }

                        val itemlist = keyhash[args[1]]

                        if (itemlist != null) {
                            for (i in 0 until itemlist.size){

                                if (i == 0){
                                    p.sendMessage("$prefix§e" + args[1] + "§aのカギ一覧")
                                }

                                val item: ItemStack = itemlist[i]

                                p.sendMessage("§b$i§f:" + item.toString())

                            }
                            return true
                        }
                    }

                    if (args!![0].equals("keyadd", ignoreCase = true)){

                        if (!namehash.containsValue(args[1])){
                            p.sendMessage("$prefix§cその名前は登録されていません")
                            return true
                        }

                        val itemlist = keyhash[args[1]]

                        val item: ItemStack = p.inventory.itemInMainHand

                        itemlist!!.add(item)

                        for (entry in keyhash.entries){
                            config!!.getConfig()!!.set("keyitem." + entry.key, entry.value)
                        }

                        config!!.saveConfig()

                        config!!.reloadConfig()

                        keyhash.clear()
                        namehash.clear()
                        drophash.clear()

                        for (key in config!!.getConfig()!!.getConfigurationSection("keyitem").getKeys(false)) {
                            keyhash.put(key, config!!.getConfig()!!.getList("keyitem.$key") as MutableList<ItemStack>)
                        }

                        for (key in config!!.getConfig()!!.getConfigurationSection("name").getKeys(false)) {
                            namehash.put(key, config!!.getConfig()!!.getString("name.$key"))
                        }

                        for (key in config!!.getConfig()!!.getConfigurationSection("drop").getKeys(false)) {
                            drophash.put(key, config!!.getConfig()!!.getBoolean("drop.$key"))
                        }

                        if (item.hasItemMeta()) {
                            p.sendMessage("$prefix§e" + args[1] + "§aに§b${item.itemMeta.displayName}§aを追加しました")
                        }else{
                            p.sendMessage("$prefix§e" + args[1] + "§aに§b$item§aを追加しました")
                        }

                        return true

                    }

                    if(args!![0].equals("remove", ignoreCase = true)){

                        if (!namehash.containsValue(args[1])){
                            p.sendMessage("$prefix§cその名前は登録されていません")
                            return true
                        }

                        namehash.values.remove(args[1])
                        keyhash.remove(args[1])
                        drophash.remove(args[1])

                        for (key in getConfig().getKeys(false)) {
                            config!!.getConfig()!!.set(key, null)
                        }

                        config!!.saveConfig()

                        for (entry in keyhash.entries){
                            config!!.getConfig()!!.set("keyitem." + entry.key, entry.value)
                        }

                        for (entry in namehash.entries){
                            config!!.getConfig()!!.set("name." + entry.key, entry.value)
                        }

                        for (entry in drophash.entries){
                            config!!.getConfig()!!.set("drop." + entry.key, entry.value)
                        }

                        config!!.saveConfig()

                        p.sendMessage("$prefix§e${args[1]}§aを削除しました")

                        return true
                    }

                }

                3 -> {

                    if (args!![0].equals("keyremove")){

                        if (!namehash.containsValue(args[1])){
                            p.sendMessage("$prefix§cその名前は登録されていません")
                            return true
                        }

                        val index: Int

                        val itemlist = keyhash[args[1]]

                        try {
                            index = Integer.parseInt(args[2])

                            if (index < 0 || index > itemlist!!.size){
                                sender.sendMessage("$prefix§c正しい番号を指定してください")
                                return true
                            }

                        } catch (ms: NumberFormatException) {

                            sender.sendMessage("$prefix§c番号を指定してください")
                            return true

                        }

                        itemlist!!.removeAt(index)

                        config!!.getConfig()!!.set("keyitem", null)

                        for (entry in keyhash.entries){
                            config!!.getConfig()!!.set("keyitem." + entry.key, entry.value)
                        }

                        config!!.saveConfig()

                        p.sendMessage("$prefix§e${args[1]}§aから${index}のカギを削除しました")

                        return true

                    }

                    if (args!![0].equals("dropset", ignoreCase = true)){

                        if (!namehash.containsValue(args[1])){
                            p.sendMessage("$prefix§cその名前は登録されていません")
                            return true
                        }

                        var drop: Boolean

                        try {

                            drop = java.lang.Boolean.valueOf(args[2])!!

                        }catch (ms: ClassCastException){
                            p.sendMessage("$prefix§etrue§cか§efalse§cで選んでください")
                            return true
                        }

                        drophash[args[1]] = drop

                        for (entry in drophash.entries){
                            config!!.getConfig()!!.set("drop." + entry.key, entry.value)
                        }

                        config!!.saveConfig()

                        config!!.reloadConfig()

                        keyhash.clear()
                        namehash.clear()
                        drophash.clear()

                        for (key in config!!.getConfig()!!.getConfigurationSection("keyitem").getKeys(false)) {
                            keyhash[key] = config!!.getConfig()!!.getList("keyitem.$key") as MutableList<ItemStack>
                        }

                        for (key in config!!.getConfig()!!.getConfigurationSection("name").getKeys(false)) {
                            namehash[key] = config!!.getConfig()!!.getString("name.$key")
                        }

                        for (key in config!!.getConfig()!!.getConfigurationSection("drop").getKeys(false)) {
                            drophash.put(key, config!!.getConfig()!!.getBoolean("drop.$key"))
                        }

                        p.sendMessage("$prefix§e${args[1]}§aのカギ消費設定を${drop}にしました")

                        return true
                    }

                }

            }

            p.sendMessage("$prefix§c使い方が間違ってます")

        }

        return false
    }

    @EventHandler
    fun onDoorOpen(e: PlayerInteractEvent){

        if (e.action != Action.RIGHT_CLICK_BLOCK)return

        if (e.clickedBlock.type != Material.DARK_OAK_DOOR && e.clickedBlock.type != Material.ACACIA_DOOR && e.clickedBlock.type != Material.BIRCH_DOOR && e.clickedBlock.type != Material.IRON_DOOR && e.clickedBlock.type != Material.JUNGLE_DOOR && e.clickedBlock.type != Material.SPRUCE_DOOR && e.clickedBlock.type != Material.WOODEN_DOOR)return

        var clickedBlock = e.clickedBlock

        val loc = e.clickedBlock.location

        val location = "${loc.blockX}/" + loc.blockY + "/" + loc.blockZ + "/" + loc.world.name

        val location2 = "${loc.blockX}/" + (loc.blockY-1) + "/" + loc.blockZ + "/" + loc.world.name

        var name = namehash[location2]

        if (!namehash.keys.contains(location2)) {

            name = namehash[location]

            if (!namehash.keys.contains(location)) {
                return
            }
        }

        val p = e.player

        val key = keyhash[name]

        val drop = drophash[name]

        for (i in 0 until key!!.size){

            if (p.inventory.itemInMainHand == key[i]) {
                p.sendMessage("$prefix§a鍵が認証されました。")

                if (clickedBlock.getRelative(BlockFace.DOWN).getType().equals(clickedBlock.getType()))
                {
                    clickedBlock = clickedBlock.getRelative(BlockFace.DOWN);
                }

                if (drop!!){

                    p.inventory.itemInMainHand.amount = 0

                }

                object : BukkitRunnable() {
                    override fun run() {

                        val state = clickedBlock.getState()

                        if (state != null) {
                            val data = state!!.getData()

                            if (data != null) {
                                if (data is Openable) {
                                    val door = data as Openable

                                    // this is the point of the whole plugin right here.
                                    if (door.isOpen) {
                                        door.isOpen = false
                                        state!!.update()
                                    }
                                } else {
                                    // to be useful, this should probably include material information
                                    Bukkit.getLogger().warning("Tried to close the block, but instanceof Openable check failed.")
                                }
                            }
                        }

                    }
                }.runTaskLater(this, 10)

                return
            }

        }

        p.sendMessage("$prefix§c鍵が違います")
        e.isCancelled = true

        return

    }

    @EventHandler
    fun onRedstoneEvent(e: BlockRedstoneEvent){

        val loc = e.block.location

        val location = "${loc.blockX}/" + loc.blockY + "/" + loc.blockZ + "/" + loc.world.name

        val location2 = "${loc.blockX}/" + (loc.blockY-1) + "/" + loc.blockZ + "/" + loc.world.name

        if (!namehash.keys.contains(location2)) {
            if (!namehash.keys.contains(location)) {
                return
            }
        }

        e.newCurrent = e.getOldCurrent();

    }

}
