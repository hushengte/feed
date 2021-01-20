DROP TABLE IF EXISTS `lib_book`;
DROP TABLE IF EXISTS `lib_publisher`;
CREATE TABLE `lib_publisher` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(40) NOT NULL,
  `place` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`id`)
);
insert into `lib_publisher`(`id`,`name`,`place`) values 
(1,'Wiley','Hoboken, NJ'),
(2,'OReilly','Sebastopol, CA'),
(3,'Apress','Sebastopol, CA'),
(4,'Sams','Sebastopol, CA'),
(5,'John Wiley','Hoboken, NJ'),
(6,'Prentice Hall','Sebastopol, CA'),
(7,'Libraries Unlimited','Westport, Conn.');

CREATE TABLE `lib_book` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `call_number` varchar(50) DEFAULT NULL,
  `collation` varchar(30) DEFAULT NULL,
  `create_date` timestamp DEFAULT NULL,
  `ebook` varchar(255) DEFAULT NULL,
  `isbn` varchar(20) DEFAULT NULL,
  `last_update` timestamp DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `notes` varchar(200) DEFAULT NULL,
  `publish_year` varchar(15) DEFAULT NULL,
  `serial_name` varchar(30) DEFAULT NULL,
  `publisher_id` int(11) DEFAULT NULL,
  `author` varchar(512) DEFAULT NULL,
  `subject` varchar(1024) DEFAULT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK_q22apv1r2esehe42sqfa24yua` FOREIGN KEY (`publisher_id`) REFERENCES `lib_publisher` (`id`)
);
insert into `lib_book` (`id`, `call_number`, `collation`, `create_date`, `ebook`, `isbn`, `last_update`, `name`, `notes`, `publish_year`, 
	`serial_name`, `publisher_id`, `author`, `subject`) values
('1','BS2585.3','266页-1版','2015-04-14 00:00:00',NULL,'4892511005478','2011-08-16 00:00:00','仁者无惧：路加福音',NULL,'2005年',
	'生命更新解经系列','1','[{\"names\":[\"威尔斯比\"],\"level\":\"著\"},{\"names\":[\"苇默\"],\"level\":\"译\"}]','[\"Bible.N.T.Luke\",\"路加福音\",\"Study and teaching\",\"研究与教导\"]'),
('2','BS2585.5CAI','195页-1版','2015-04-14 00:00:00',NULL,'','2012-12-04 00:00:00','马可福音要义',NULL,'1992',
	'','1','[{\"names\":[\"蔡恺真\"],\"level\":\"著\"}]','[\"Bible.N.T.Mark-study and teaching\",\"圣经-新约-马可福音-学习与教学\",\"Bible.N.T.Mark-sermons\",\"圣经-新约-马可福音- 讲章\"]'),
('3','RC489.G4','466页-1版','2015-04-14 00:00:00',NULL,'9787561436820','2011-11-11 00:00:00','完形治疗：观点与应用',NULL,'2007年',
	'心理治疗系列','2','[{\"names\":[\"埃德温·尼维斯（Edwin C.Nevis）\"],\"level\":\"主编\"},{\"names\":[\"何丽仪\",\"蔡瑞峰\",\"黄近南\"],\"level\":\"译\"}]','[\"Gestalt Therapy\",\"完形治疗\"]'),
('4','RJ505.P6','359页-1版','2015-04-14 00:00:00',NULL,'9787561436622','2011-11-30 00:00:00','游戏治疗技巧',NULL,'2007年',
	'心理治疗系列','2','[{\"names\":[\"卡吉洛西（Cangelosi\",\"Donna M.）\",\"斯卡夫（Schaefer\",\"Charles E.）\"],\"level\":\"编著\"},{\"names\":[\"何长珠\"],\"level\":\"译\"}]','[\"游戏疗法\",\"Play therapy\",\"方法论\",\"Methodology\"]'),
('5','HM251','504页-8版','2015-04-14 00:00:00',NULL,'9787115138804','2011-11-11 00:00:00','社会心理学',NULL,'2010年',
	'','3','[{\"names\":[\"戴维·迈尔斯（David G.Myers）\"],\"level\":\"著\"},{\"names\":[\"乐国安\",\"侯玉波等\",\"张智勇\"],\"level\":\"译\"}]','[\"Social Psychology\",\"社会心理学\"]'),
('6','BL1802','293页-1版','2015-04-14 00:00:00',NULL,'9787801901743','2011-11-11 00:00:00','中国人的宗教心理：宗教认同的理论分析与实证研究',NULL,'2004年',
	'','3','[{\"names\":[\"梁丽萍\"],\"level\":\"著\"}]','[\"心理学\",\"中国\",\"Psychology\",\"China\",\"宗教\",\"宗教的\",\"Religious\",\"religion\",\"心理学-宗教的\"]'),
('7','BF51','331页','2015-04-14 00:00:00','26-宗教心理学.pdf','9787300069272','2011-11-14 00:00:00','宗教心理学导论',NULL,'2005年',
	'宗教学译丛','4','[{\"names\":[\"[英]麦克·阿盖尔（Michael Argyle）\"],\"level\":\"著\"},{\"names\":[\"陈彪\"],\"level\":\"译\"}]','[\"心理学\",\"Psychology\",\"心理学与宗教\",\"Psychology and religion(c)\",\"Religious\",\"宗教的\"]'),
('8','HM251','183页-1版','2015-04-14 00:00:00',NULL,'9787801093660','2011-11-14 00:00:00','乌合之众：大众心理研究',NULL,'2004年',
	'社会学经典读本','5','[{\"names\":[\"[法]古斯塔夫·勒庞\"],\"level\":\"著\"},{\"names\":[\"冯克利\"],\"level\":\"译\"}]','[\"Social Psychology\",\"社会心理学\"]'),
('9','BJ53','179页-1版','2015-04-14 00:00:00','29-[现代西方学术文库]26新教伦理与资本主义精神.pdf','9787561323533','2011-11-14 00:00:00','新教伦理与资本主义精神',NULL,'2002年',
	'西方学术经典译丛','6','[{\"names\":[\"[德]马克斯·韦伯（Max Weber）\"],\"level\":\"著\"},{\"names\":[\"黄晓京\"],\"level\":\"译\"}]','[\"ethics\",\"伦理道德\"]'),
('10','B29','128页-1版','2015-04-14 00:00:00','30-重返理性.pdf','9787301078501','2011-11-15 00:00:00','重返理性：对启蒙运动证据主义的批判以及为理性与信仰上帝的辩护',NULL,'2008年',
	'未名译库·哲学与宗教系列','7','[{\"names\":[\"凯利·詹姆斯·克拉克（Kelly James Clark）\"],\"level\":\"著\"},{\"names\":[\"唐安\"],\"level\":\"译\"}]','[\"Philsophy\",\"哲学\"]'),
('11','B3318.C35 NIE 2001 C','290页-1版','2015-04-14 00:00:00','31-尼采与基督教思想-尼采等著-道风书社-2001-buck.pdf','9628322362','2015-06-06 13:08:49','尼采与基督教思想',NULL,'2001年',
	'历代基督教思想学术文库','4','[{\"names\":[\"尼采（Friedrich Nietzsche）\",\"沃格林（Eric Voegelin）等\",\"洛维特（Kari Lowith）\"],\"level\":\"著\"},{\"names\":[\"吴增定\",\"李猛\",\"田立年\"],\"level\":\"译\"}]','[\"Theology\",\"神学\",\"Doctrine\",\"教条的\"]'),
('12','B522','709页-1','2015-04-14 00:00:00',NULL,'9787806436769','2011-11-15 00:00:00','西方哲学史 第一卷：总论',NULL,'2005年',
	'','5','[{\"names\":[\"叶秀山\",\"王树人\"],\"level\":\"著\"}]','[\"Philosophy\",\"哲学史\",\"History\"]');

DROP TABLE IF EXISTS `id_increment_generator`;
CREATE TABLE `id_increment_generator` (
  `sequence_name` varchar(255) DEFAULT NULL,
  `next_val` int(11) DEFAULT NULL
);
insert into `id_increment_generator`(`sequence_name`,`next_val`) values 
('lib_publisher',8),
('lib_book',13);
