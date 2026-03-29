/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GNU General Public License was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public final class GuiMainMenuVisitor extends WurstClassVisitor
{
	private final String buttonListName;

	public GuiMainMenuVisitor(ClassVisitor cv, boolean obf)
	{
		super(cv);

		String guiButton = unmap("net/minecraft/client/gui/GuiButton");
		String initGuiName = mapMethodName("net/minecraft/client/gui/GuiMainMenu",
			"initGui", "()V");
		String actionPerformedName = mapMethodName(
			"net/minecraft/client/gui/GuiMainMenu", "actionPerformed",
			"(L" + guiButton + ";)V");

		buttonListName = mapFieldName("net/minecraft/client/gui/GuiMainMenu",
			"buttonList", "Ljava/util/List;");

		registerMethodVisitor(initGuiName, "()V",
			mv -> new InitGuiVisitor(mv));
		registerMethodVisitor(actionPerformedName, "(L" + guiButton + ";)V",
			mv -> new ActionPerformedVisitor(mv));
	}

	private final class InitGuiVisitor extends MethodVisitor
	{
		private InitGuiVisitor(MethodVisitor mv)
		{
			super(Opcodes.ASM4, mv);
		}

		@Override
		public void visitInsn(int opcode)
		{
			if(opcode == Opcodes.RETURN)
			{
				mv.visitVarInsn(Opcodes.ALOAD, 0);
				mv.visitVarInsn(Opcodes.ALOAD, 0);
				mv.visitFieldInsn(Opcodes.GETFIELD,
					"net/minecraft/client/gui/GuiMainMenu", buttonListName,
					"Ljava/util/List;");
				mv.visitMethodInsn(Opcodes.INVOKESTATIC,
					"net/wurstclient/forge/compatibility/WEventFactory",
					"onGuiMainMenuInit",
					"(Lnet/minecraft/client/gui/GuiScreen;Ljava/util/List;)V",
					false);
			}

			super.visitInsn(opcode);
		}
	}

	private static final class ActionPerformedVisitor extends MethodVisitor
	{
		private ActionPerformedVisitor(MethodVisitor mv)
		{
			super(Opcodes.ASM4, mv);
		}

		@Override
		public void visitInsn(int opcode)
		{
			if(opcode == Opcodes.RETURN)
			{
				mv.visitVarInsn(Opcodes.ALOAD, 1);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC,
					"net/wurstclient/forge/compatibility/WEventFactory",
					"onGuiMainMenuButtonPress",
					"(Lnet/minecraft/client/gui/GuiButton;)V", false);
			}

			super.visitInsn(opcode);
		}
	}
}
