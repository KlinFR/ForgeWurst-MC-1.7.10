/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GNU General Public License was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public final class GuiChatVisitor extends WurstClassVisitor
{
	public GuiChatVisitor(ClassVisitor cv, boolean obf)
	{
		super(cv);

		registerMethodVisitor("keyTyped", "(CI)V", mv -> new KeyTypedVisitor(mv));
		registerMethodVisitor("func_73869_a", "(CI)V",
			mv -> new KeyTypedVisitor(mv));
		registerMethodVisitor("a", "(CI)V", mv -> new KeyTypedVisitor(mv));
		registerMethodVisitor("sendChatMessage", "(Ljava/lang/String;)V",
			mv -> new SendChatMessageVisitor(mv));
		registerMethodVisitor("func_146403_a", "(Ljava/lang/String;)V",
			mv -> new SendChatMessageVisitor(mv));
		registerMethodVisitor("a", "(Ljava/lang/String;)V",
			mv -> new SendChatMessageVisitor(mv));
	}

	private static final class KeyTypedVisitor extends MethodVisitor
	{
		private KeyTypedVisitor(MethodVisitor mv)
		{
			super(Opcodes.ASM4, mv);
		}

		@Override
		public void visitCode()
		{
			super.visitCode();

			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitVarInsn(Opcodes.ILOAD, 2);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
				"net/wurstclient/forge/compatibility/WEventFactory",
				"onGuiChatTabComplete",
				"(Lnet/minecraft/client/gui/GuiScreen;I)Z", false);
			Label continueLabel = new Label();
			mv.visitJumpInsn(Opcodes.IFEQ, continueLabel);
			mv.visitInsn(Opcodes.RETURN);
			mv.visitLabel(continueLabel);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		}
	}

	private static final class SendChatMessageVisitor extends MethodVisitor
	{
		private SendChatMessageVisitor(MethodVisitor mv)
		{
			super(Opcodes.ASM4, mv);
		}

		@Override
		public void visitCode()
		{
			super.visitCode();

			mv.visitVarInsn(Opcodes.ALOAD, 1);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
				"net/wurstclient/forge/compatibility/WEventFactory",
				"onClientSentMessage", "(Ljava/lang/String;)Z", false);
			Label continueLabel = new Label();
			mv.visitJumpInsn(Opcodes.IFNE, continueLabel);
			mv.visitInsn(Opcodes.RETURN);
			mv.visitLabel(continueLabel);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		}
	}
}
