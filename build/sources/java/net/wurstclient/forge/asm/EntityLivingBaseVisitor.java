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

public final class EntityLivingBaseVisitor extends WurstClassVisitor
{
	public EntityLivingBaseVisitor(ClassVisitor cv, boolean obf)
	{
		super(cv);

		String jumpDesc = "()V";
		registerMethodVisitor("jump", jumpDesc, mv -> new JumpVisitor(mv));
		registerMethodVisitor("func_70664_aZ", jumpDesc,
			mv -> new JumpVisitor(mv));
	}

	private static final class JumpVisitor extends MethodVisitor
	{
		private JumpVisitor(MethodVisitor mv)
		{
			super(Opcodes.ASM4, mv);
		}

		@Override
		public void visitCode()
		{
			super.visitCode();

			Label continueLabel = new Label();
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitTypeInsn(Opcodes.INSTANCEOF,
				"net/minecraft/entity/player/EntityPlayer");
			mv.visitJumpInsn(Opcodes.IFEQ, continueLabel);
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitTypeInsn(Opcodes.CHECKCAST,
				"net/minecraft/entity/player/EntityPlayer");
			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
				"net/wurstclient/forge/compatibility/WEventFactory",
				"entityPlayerJump",
				"(Lnet/minecraft/entity/player/EntityPlayer;)Z", false);
			mv.visitJumpInsn(Opcodes.IFNE, continueLabel);
			mv.visitInsn(Opcodes.RETURN);
			mv.visitLabel(continueLabel);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		}
	}
}
