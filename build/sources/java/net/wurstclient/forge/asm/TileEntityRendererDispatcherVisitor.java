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

public final class TileEntityRendererDispatcherVisitor extends WurstClassVisitor
{
	public TileEntityRendererDispatcherVisitor(ClassVisitor cv, boolean obf)
	{
		super(cv);

		registerMethodVisitor("renderTileEntity",
			"(Lnet/minecraft/tileentity/TileEntity;F)V",
			mv -> new RenderVisitor(mv));
		registerMethodVisitor("func_147544_a",
			"(Lnet/minecraft/tileentity/TileEntity;F)V",
			mv -> new RenderVisitor(mv));
		registerMethodVisitor("a", "(Lnet/minecraft/tileentity/TileEntity;F)V",
			mv -> new RenderVisitor(mv));
	}

	private static final class RenderVisitor extends MethodVisitor
	{
		private RenderVisitor(MethodVisitor mv)
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
				"renderTileEntity",
				"(Lnet/minecraft/tileentity/TileEntity;)Z", false);
			Label continueLabel = new Label();
			mv.visitJumpInsn(Opcodes.IFNE, continueLabel);
			mv.visitInsn(Opcodes.RETURN);
			mv.visitLabel(continueLabel);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		}
	}
}
