package com.wendy.work.camera1.util;

/**
 * Created by Administrator on 2015/10/29.
 */
public class InterpretationConvert {
    public static String ConvertToHtml(String interpretation){
        int index1=interpretation.indexOf("����");
        int index2=interpretation.indexOf("ƴ��");
        int index3=interpretation.indexOf("���");
        int index4=interpretation.indexOf("�ʻ���");
        int index5=interpretation.indexOf("����");
        int index6=interpretation.indexOf("��˳���");
        if(-1==index1||-1==index2||-1==index3||-1==index4)
            return interpretation;
        String part1=interpretation.substring(index1+3, index2).replace("\n", "<br>");//����
        String part2=interpretation.substring(index2+3,index3).replace("\n", "<br>");//ƴ��
        String part3=interpretation.substring(index3+3,index4).replace("\n", "<br>");//���
        String part4=interpretation.substring(index4+4,index5).replace("\n", "<br>");//�ʻ���
        String part5=interpretation.substring(index5+3,index6).replace("\n", "<br>");//����
        String part6=interpretation.substring(index6+5,interpretation.length()).replace("\n","");//��˳���
        String htmlPart1=String.format("<p>" +
                "<strong >������</strong>" +
                "%s" +
                "</p>", part1);
        String htmlPart2=String.format("<p>" +
                "<strong >ƴ����</strong>" +
                "%s" +
                "</p>",part2);
        String htmlPart3=String.format("<p>" +
                "<strong >��飺</strong><br>" +
                "%s" +
                "</p>",part3);
        String htmlPart4=String.format("<p>" +
                "<strong >�ʻ�����</strong>" +
                "%s" +
                "</p>",part4);
        String htmlPart5=String.format("<p>" +
                "<strong >���ף�</strong>" +
                "%s" +
                "</p>",part5);
        String htmlPart6=String.format("<p>" +
                "<strong >��˳��ţ�</strong>" +
                "%s" +
                "</p>",part6);
        String showHtml=htmlPart1+htmlPart2+htmlPart3+htmlPart4+htmlPart5+htmlPart6;
        return showHtml;
    }
}
