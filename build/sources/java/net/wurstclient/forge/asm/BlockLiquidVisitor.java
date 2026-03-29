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

public final class BlockLiquidVisitor extends WurstClassVisitor
{
	public BlockLiquidVisitor(ClassVisitor cv, boolean obf)
	{
		super(cv);

		String desc =
			"(Lnet/minecraft/world/World;III)Lnet/minecraft/util/AxisAlignedBB;";

		registerMethodVisitor("getCollisionBoundingBoxFromPool", desc,
			mv -> new GetCollisionBoundingBoxVisitor(mv));
		registerMethodVisitor("func_149668_a", desc,
			mv -> new GetCollisionBoundingBoxVisitor(mv));
		registerMethodVisitor("a", desc,
			mv -> new GetCollisionBoundingBoxVisitor(mv));
	}

	private static final class GetCollisionBoundingBoxVisitor
		extends MethodVisitor
	{
		private GetCollisionBoundingBoxVisitor(MethodVisitor mv)
		{
			super(Opcodes.ASM4, mv);
		}

		@Override
		public void visitCode()
		{
			super.visitCode();

			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
				"net/wurstclient/forge/compatibility/WEventFactory",
				"shouldLiquidBeSolid", "(Lnet/minecraft/block/Block;)Z",
				false);
			Label continueLabel = new Label();
			mv.visitJumpInsn(Opcodes.IFEQ, continueLabel);

			mv.visitVarInsn(Opcodes.ILOAD, 2);
			mv.visitInsn(Opcodes.I2D);
			mv.visitVarInsn(Opcodes.ILOAD, 3);
			mv.visitInsn(Opcodes.I2D);
			mv.visitVarInsn(Opcodes.ILOAD, 4);
			mv.visitInsn(Opcodes.I2D);
			mv.visitVarInsn(Opcodes.ILOAD, 2);
			mv.visitInsn(Opcodes.I2D);
			mv.visitInsn(Opcodes.DCONST_1);
			mv.visitInsn(Opcodes.DADD);
			mv.visitVarInsn(Opcodes.ILOAD, 3);
			mv.visitInsn(Opcodes.I2D);
			mv.visitInsn(Opcodes.DCONST_1);
			mv.visitInsn(Opcodes.DADD);
			mv.visitVarInsn(Opcodes.ILOAD, 4);
			mv.visitInsn(Opcodes.I2D);
			mv.visitInsn(Opcodes.DCONST_1);
			mv.visitInsn(Opcodes.DADD);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
				"net/minecraft/util/AxisAlignedBB", "getBoundingBox",
				"(DDDDDD)Lnet/minecraft/util/AxisAlignedBB;", false);
			mv.visitInsn(Opcodes.ARETURN);

			mv.visitLabel(continueLabel);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		}
	}
}
