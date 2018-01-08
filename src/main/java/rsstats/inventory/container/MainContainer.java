package rsstats.inventory.container;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import rsstats.data.ExtendedPlayer;
import rsstats.inventory.SkillsInventory;
import rsstats.inventory.StatsInventory;
import rsstats.inventory.WearableInventory;
import rsstats.inventory.slots.SkillSlot;
import rsstats.inventory.slots.StatSlot;
import rsstats.items.SkillItem;
import rsstats.items.StatItem;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author rares
 */
public class MainContainer extends Container {
    private final EntityPlayer player;
    private final InventoryPlayer inventoryPlayer;
    private final StatsInventory statsInventory;
    private final WearableInventory wearableInventory;

    private final SkillsInventory skillsInventory;

    public MainContainer(EntityPlayer player, InventoryPlayer inventoryPlayer, StatsInventory statsInventory, SkillsInventory skillsInventory, WearableInventory wearableInventory) {
        this.player = player;
        this.inventoryPlayer = inventoryPlayer;
        this.statsInventory = statsInventory;
        this.skillsInventory = skillsInventory;
        this.wearableInventory = wearableInventory;
        addSlots();
    }


    public MainContainer() {
        this.player = null;
        this.inventoryPlayer = null;
        this.statsInventory = null;
        this.skillsInventory = null;
        this.wearableInventory = null;
    }
    
    private void addSlots() {
        /*if (inventoryPlayer != null)
            for (int y = 0; y < 3; ++y) {
                for (int x = 0; x < 9; ++x) {
                    this.addSlotToContainer(new Slot(inventoryPlayer, x + y * 9 + 9, 8 + x * 18, 84 + y * 18));
                }
            }*/

        // Расставляем слоты на панели руки
        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(inventoryPlayer, i, (i*18 -3) +8, 188));
        }

        // Расставляем слоты на панели статов
        for (int i = 0, slotIndex = 0; i < statsInventory.getSizeInventory(); ++i, slotIndex++) {
            this.addSlotToContainer(new StatSlot(statsInventory, i, (i*18 +167) +8, /*-24*/8));
            //this.addSlotToContainer(new StatSlot(statsInventory, slotIndex, i*9, 0));
        }

        // Расставляем слоты на панели скиллов
        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x) {
                this.addSlotToContainer(new SkillSlot(skillsInventory, x + y * 9 /*+ 9*/, (x*18 +167) +8, (y * 18) + 26));
            }
        }

        // Расставляем слоты для брони
        for (int i = 0; i < 4; ++i)
        {
            final int k = i;
            this.addSlotToContainer(new Slot(inventoryPlayer, inventoryPlayer.getSizeInventory() - 1 - i, (i*18 + 51) +8, 8)
            {

                @Override
                public int getSlotStackLimit() { return 1; }
                @Override
                public boolean isItemValid(ItemStack par1ItemStack)
                {
                    if (par1ItemStack == null) return false;
                    return par1ItemStack.getItem().isValidArmor(par1ItemStack, k, player);
                }
                @SideOnly(Side.CLIENT)
                public IIcon getBackgroundIconIndex() {
                    return ItemArmor.func_94602_b(k);
                }

            });
        }

        // Расставляем слоты на панели носимых вещей
        for (int y = 0; y < 4; ++y) {
            for (int x = 0; x < 4; ++x) {
                this.addSlotToContainer(new Slot(wearableInventory, x + y * 4 /*+ 9*/, (x*18 + 51) +8, (y * 18) + 26)); //8
            }
        }
    }

    /**
     * This should always return true, since custom inventory can be accessed from anywhere
     * @param player TODO
     * @return TODO
     */
    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
    
    /**
     * Called when a entityPlayer shift-clicks on a slot. You must override this or you will crash when someone does that.
     * Basically the same as every other container I make, since I define the same constant indices for all of them
     * @param player TODO
     * @param par2 TODO
     * @return TODO
     */
    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int par2) {
        ItemStack itemstack = null;
        Slot slot = (Slot) this.inventorySlots.get(par2);
        return itemstack;
    }

    @Override
    public ItemStack slotClick(int slotId, int clickedButton, int mode, EntityPlayer playerIn) {
        Slot slot;
        try {
            slot = getSlot(slotId);
        } catch(Exception e) {
            return super.slotClick(slotId, clickedButton, mode, playerIn);
            //return null; // костыль
        }
        Item itemInSlot;
        if (slot.getStack() != null && slot.getStack().getItem() != null) {
            itemInSlot = slot.getStack().getItem();
        } else {
            return super.slotClick(slotId, clickedButton, mode, playerIn);
            //return null;
        }

        // Прокачка навыков
        List subitems = new ArrayList();
        itemInSlot.getSubItems(itemInSlot, CreativeTabs.tabMaterials, subitems);
        if (clickedButton == 1) { // ПКМ
            int damage = itemInSlot.getDamage(slot.getStack());
            itemInSlot.setDamage(slot.getStack(), damage < subitems.size()-1 ? damage+1 : subitems.size()-1);
            ExtendedPlayer.get(playerIn).updateParams();
            return null;
        }
        if (clickedButton == 2) { // СКМ
            int damage = itemInSlot.getDamage(slot.getStack());
            itemInSlot.setDamage(slot.getStack(), damage > 0 ? damage-1 : 0);
            ExtendedPlayer.get(playerIn).updateParams();
            return null;
        }

        if ((slot.inventory == statsInventory || slot.inventory == skillsInventory) && (itemInSlot instanceof SkillItem || itemInSlot instanceof StatItem)) {
            ItemStack itemStack = getSlot(slotId).getStack();

            // Защита от дублирующихся сообщений в чате
            if (!playerIn.worldObj.isRemote) {
                ( (StatItem) itemStack.getItem() ).roll(itemStack, playerIn);
            }
            return null;
        }
        return super.slotClick(slotId, clickedButton, mode, playerIn);
    }

    public SkillsInventory getSkillsInventory() {
        return skillsInventory;
    }
}
