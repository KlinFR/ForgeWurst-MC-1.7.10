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

public final class RenderBlocksVisitor extends WurstClassVisitor
{
	public RenderBlocksVisitor(ClassVisitor cv, boolean obf)
	{
		super(cv);

		String blockDesc = "(Lnet/minecraft/block/Block;III)Z";
		String blockModelDesc = "(Lnet/minecraft/block/Block;IIIFFF)Z";

		registerMethodVisitor("renderBlockByRenderType", blockDesc,
			mv -> new RenderBlockVisitor(mv));
		registerMethodVisitor("func_147805_b", blockDesc,
			mv -> new RenderBlockVisitor(mv));
		registerMethodVisitor("b", blockDesc, mv -> new RenderBlockVisitor(mv));

		registerMethodVisitor("renderStandardBlock", blockDesc,
			mv -> new RenderBlockModelVisitor(mv));
		registerMethodVisitor("func_147784_q", blockDesc,
			mv -> new RenderBlockModelVisitor(mv));
		registerMethodVisitor("q", blockDesc,
			mv -> new RenderBlockModelVisitor(mv));
		registerMethodVisitor("renderStandardBlockWithAmbientOcclusion",
			blockModelDesc, mv -> new RenderBlockModelVisitor(mv));
		registerMethodVisitor("func_147751_a", blockModelDesc,
			mv -> new RenderBlockModelVisitor(mv));
		registerMethodVisitor("a", blockModelDesc,
			mv -> new RenderBlockModelVisitor(mv));
		registerMethodVisitor(
			"renderStandardBlockWithAmbientOcclusionPartial", blockModelDesc,
			mv -> new RenderBlockModelVisitor(mv));
		registerMethodVisitor("func_147808_b", blockModelDesc,
			mv -> new RenderBlockModelVisitor(mv));
		registerMethodVisitor("renderStandardBlockWithColorMultiplier",
			blockModelDesc, mv -> new RenderBlockModelVisitor(mv));
		registerMethodVisitor("func_147736_d", blockModelDesc,
			mv -> new RenderBlockModelVisitor(mv));
	}

	private static final class RenderBlockVisitor extends MethodVisitor
	{
		private RenderBlockVisitor(MethodVisitor mv)
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
				"renderBlockByRenderType",
				"(Lnet/minecraft/block/Block;)Z", false);
			Label continueLabel = new Label();
			mv.visitJumpInsn(Opcodes.IFNE, continueLabel);
			mv.visitInsn(Opcodes.ICONST_0);
			mv.visitInsn(Opcodes.IRETURN);
			mv.visitLabel(continueLabel);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		}
	}

	private static final class RenderBlockModelVisitor extends MethodVisitor
	{
		private RenderBlockModelVisitor(MethodVisitor mv)
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
				"renderBlockModel", "(Lnet/minecraft/block/Block;)Z", false);
			Label continueLabel = new Label();
			mv.visitJumpInsn(Opcodes.IFNE, continueLabel);
			mv.visitInsn(Opcodes.ICONST_0);
			mv.visitInsn(Opcodes.IRETURN);
			mv.visitLabel(continueLabel);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		}
	}
}
