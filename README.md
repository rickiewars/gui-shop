# About this fork

This is a fork of the original [GUI Shop](https://github.com/UnsafeDodo/gui-shop) mod by [UnsafeDodo](https://github.com/UnsafeDodo).
I adapted it to my requirements and added some features.
I'm not planning to provide support _(at this time)_, but feel free to use or fork it if you want.

# GUI Shop

A fabric server-side mod to create and manage GUI shops.
They can be later opened by using commands, allowing integration with NPC mods like [Taterzens](https://www.curseforge.com/minecraft/mc-mods/taterzens).
<br>The mod supports [LuckPerms](https://www.curseforge.com/minecraft/mc-mods/luckperms) for permissions.

## Installation
Put the .jar file in the "mods" folder

**(Requires [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api) and (optionally) [a supported Economy](#supported-economies))**
<br><br>

## Commands and permissions
All commands can be used by admins (permission level 3) or by users/groups with the specific permission


| Description                | Command                                                                                | Permission               | 
|----------------------------|----------------------------------------------------------------------------------------|--------------------------|
| Main command               | `/guishop`                                                                             | `automessage.main`       |
| Create a shop              | `/guishop create <shopName>`                                                           | `automessage.create`     |
| Delete a shop              | `/guishop delete <shopName> `                                                          | `automessage.delete`     |
| Add an item in a shop      | `/guishop additem <shopName> <itemId> <buyPrice> <sellPrice> <currency> <description>` | `automessage.additem`    |
| Remove an item from a shop | `/guishop removeitem <shopName> <itemName>`                                            | `automessage.removeitem` |
| Open a shop for a player   | `/guishop open <shopName> <playerName>`                                                | `automessage.open`       |
| List all shops             | `/guishop list`                                                                        | `automessage.list`       |
| List all items in a shop   | `/guishop list <shopName>`                                                             | `automessage.list`       |
| Force save config          | `/guishop forcesave`                                                                   | `automessage.forcesave`  |
| Reload config file         | `/automessage reload`                                                                  | `automessage.reload`     |

### Commands examples
Create a shop: `/guishop create "Test shop"`"

Add item in a shop: `/guishop additem "Diamond" minecraft:diamond[minecraft:enchantment_glint_override=true,minecraft:custom_name=hello] 250 100 guishop:credit "This is a Diamond\\An expensive diamond\\Shiny"` *(you can split each description line by using "\\\\")*

> Item components are supported in the same way as in the `/give` command.
You can use an item generator like [mcstacker](https://mcstacker.net/) or the one from
[Gamergeeks](https://www.gamergeeks.net/apps/minecraft/give-command-generator)
to generate items with components like enchantments, custom names, etc.
In-game, you'll also get suggestions for item components.

> The buy and sell prices are without any formatting so say you configured 2 decimal places in your config
> and you want to sell an item for 1.50, you would use 150 as the sell price.

Remove item from shop: `/guishop removeitem "Test shop" "Diamond"`

Open a shop and show it to a specific player: `/guishop open "Test shop" "Steve"`


## Configuration
You can find the config file in `./config/guishop.json`
<br>Both items' names and descriptions support [Simplified Text Format](https://placeholders.pb4.eu/user/text-format/).

You can even add items from the JSON file (check [JSON Example](#json-example)). This can be useful when your `additem` command would be very long, or to easily set component data *(remember to reload the mod using `/guishop reload` after editing the config file)*


### JSON example
```json5
{
  "database": {
    "type": "sqlite",
    "currency": "./config/guishop.sqlite"
  },
  "economy": {
    "currencies": {
      "guishop:credit": {
        "name": "Credits",
        "prefix": "$",
        "suffix": "",
        "decimalPlaces": 2,
        "icon": "minecraft:diamond"
      }
    },
    "accounts": {
      "guishop:account": {
        "name": "Account",
        "currency": "guishop:credit",
        "icon": "minecraft:diamond"
      }
    }
  },
  "economyProviders": {
    "currencies": {
      "guishop:credit": "guishop"
    },
    "accounts": {
      "guishop:account": "guishop:credit"
    }
  },
  "shops": [
    {
      "shopName": "Shop number one",
      "items": [
        {
          "name": "The boat",
          "itemId": "minecraft:acacia_chest_boat",
          "description": [
            "This is a nice boat",
            "Very beautiful"
          ],
          "buyPrice": 50,
          "sellPrice": 25,
          "currency": "guishop:credit",
          "componentChanges": {}
        },
        {
          "name": "BBQ Sword",
          "itemId": "minecraft:diamond_sword",
          "description": [],
          "buyPrice": 0,
          "sellPrice": 0,
          "currency": "guishop:credit",
          "components": {
            "minecraft:enchantment_glint_override": true,
            "minecraft:custom_name": "\"hello\""
          }
        },
        {
          "name": "Amethyst",
          "itemId": "minecraft:large_amethyst_bud",
          "description": [
            "<red>Such a spectacular</red>",
            "<purple>amethyst</purple>",
            "<rainbow>SHINY</rainbow>"
          ],
          "buyPrice": 200,
          "sellPrice": 100,
          "currency": "guishop:credit",
          "components": {}
        }
      ]
    },
    {
      "shopName": "A second shop",
      "items": []
    }
  ]
}
```

## Supported Economies:
From 1.4.5 and onwards, the mod supports any (combination of) economy mod that uses the [Common Economy API](https://github.com/Patbox/common-economy-api).
A great example is [Common Bridge](https://modrinth.com/mod/common-bridge), which bridges multiple economy plugins to use the Common Economy API.
Finally, GuiShop comes with its own economy provider which can be configured in the config file.
This build-in economy provider can be configured with multiple currencies and accounts.

## Showcase
![img.png](resources/img.png)
